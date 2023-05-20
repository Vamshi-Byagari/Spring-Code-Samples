package com.vbolide.config.websocket;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import com.vbolide.config.websocket.exception.CustomStompSubProtocolErrorHandler;


/**
 * By sending Authorization Token as a query parameter with WebSocket URI.
 * we can prevent the anonymous client from connecting to websocket endpoint.
 * in is not recommended to send Authorization token as a query parameter.
 * 
 * @author Vamshi Byagari
 */

@Configuration
@EnableWebSocketMessageBroker
public class AppWebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer{

	@Value("${webscoket.endpoints}")
	private String[] websocketEndPoints;

	@Value("${websocket.allowed-origin-patterns}")
	private String[] websocketAllowedOriginPatterns;

	@Value("${websocket.simple-broker.destinations}")
	private String[] websocketSimpleBrokerDestinations;

	@Value("${weboscket.application-prefixes}")
	private String[] websocketApplicationPrefixes;


	@PostConstruct
	private void checkValues() {
		System.out.printf(
			"WEBSOCKET PROPERTIES:: websocketEndPoints: %s, websocketAllowedOriginPatterns: %s, websocketSimpleBrokerDestinations: %s, websocketApplicationPrefixes: %s%n", 
			Arrays.toString(websocketEndPoints), Arrays.toString(websocketAllowedOriginPatterns), Arrays.toString(websocketSimpleBrokerDestinations), Arrays.toString(websocketApplicationPrefixes)
		);
	}


	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
			.addEndpoint(websocketEndPoints)// [1]
			.setAllowedOriginPatterns(websocketAllowedOriginPatterns)// [2]
			.addInterceptors(customHandshakeInterceptor())// [3]
			.setHandshakeHandler(customHandshakeHandler());// [4]

		//[1] WebSocket endpoint, where client request to upgrade the HTTP to WebSocket. 
			//In general it'll be public because websocket protocol upgrade request doesn't support header to authenticate.
			//It does support request parameters, but not a recommended approach.
		//[2] For state-less application to access the endpoint.
		//[3] To retrieve the token sent as parameter and pass on to `HandlerAdapter`.
		//[4] To retrieve the Principal from the token passed on by overriding HandlerAdapter#determineUser(...)


		//EXPECTED BEHAVIOUR:: WHEN AN ERROR OCCURS WHILE PROCESSING THE STOMP COMMAND THE UNDERLYING WEBSOCKET MUST BE CLOSED
		registry.setErrorHandler(customStompSubProtocolErrorHandler());// [5]

		//[5] to handle the stomp command level errors such as `AccessDeniedException`, 
			//when a message mapping is protected with the `ADMIN` but a client with `USER` role tries to access
			//rather than closing websocket just notify the user that he does't have privileges.
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker(websocketSimpleBrokerDestinations); //destinations for the spring SimpleBroker to listen to.
		registry.setApplicationDestinationPrefixes(websocketApplicationPrefixes); //application prefix to forward the message to MessageMapping handlers.
	}

	@Override
	protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		messages.simpDestMatchers("/app/broadcast").hasRole("ADMIN"); //authorizing the client before giving access to the destinations
	}

	@Override
	protected boolean sameOriginDisabled() {
		return true; //enable when working with the state-less application
	}


	@Bean
	CustomHandshakeInterceptor customHandshakeInterceptor() {
		return new CustomHandshakeInterceptor();
	}

	@Bean
	CustomHandshakeHandler customHandshakeHandler() {
		return new CustomHandshakeHandler();
	}

	@Bean
	CustomStompSubProtocolErrorHandler customStompSubProtocolErrorHandler() {
		return new CustomStompSubProtocolErrorHandler();
	}

	@Bean
	@Lazy
	SimplMessageTemplateWrapper simplMessageTemplateWrapper() {
		//wrapper class for `SimpMessagingTemplate`, because injecting it in `AbstractSecurityWebSocketMessageBrokerConfigurer` implementaion is raising CircularDepencecyException.
		return new SimplMessageTemplateWrapper();
	}

}