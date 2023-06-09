package com.vbolide.config.websocket;

import com.vbolide.config.websocket.exception.CustomStompSubProtocolErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * By sending Authorization Token in STOMP CONNECT command header.
 * we can send Authorization token in the header.
 * anonymous clients can connect to the websocket endpoint.
 * when client send the CONNECT command we can close the websocket connection, if not authenticated.
 * 
 * @author Vamshi Byagari
 */

//@Configuration
//@EnableWebSocketMessageBroker
public class AppWebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer{

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
		registry.addEndpoint(websocketEndPoints) // [1]
			.setAllowedOriginPatterns(websocketAllowedOriginPatterns);// [2]
			//.withSockJS();

		//[1] WebSocket endpoint, where client request to upgrade the HTTP to WebSocket. 
			//In general, it'll be public because websocket protocol upgrade request doesn't support header to authenticate.
			//It does support request parameters, but not a recommended approach.
		//[2] For state-less application to access the endpoint.


		//EXPECTED BEHAVIOUR:: WHEN AN ERROR OCCURS WHILE PROCESSING THE STOMP COMMAND THE UNDERLYING WEBSOCKET MUST BE CLOSED 
		registry.setErrorHandler(customStompSubProtocolErrorHandler()); //[3] 

		//[3] to handle the stomp command level errors such as `AccessDeniedException`, 
			//when a message mapping is protected with the `ADMIN` but a client with `USER` role tries to access
			//rather than closing websocket just notify the user that he doesn't have privileges.
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
	protected void customizeClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(clientInboundChannelInterceptor());
		//it intercepts the messages coming from the client
		//Here it is used to authenticate the user with STOMP CONNECT command, the header of the command contains Authorization token.
		//if header contains the token, extract it and create Principal object and set it to StompHeaderAccessor#setUser(...)
		//if header doesn't contain the token close the wesocket connection.
	}

	@Override
	protected boolean sameOriginDisabled() {
		return true; //enable when working with the state-less application
	}

	@Bean
	CustomClientInboundChannelInterceptor clientInboundChannelInterceptor() {
		return new CustomClientInboundChannelInterceptor();
	}

	@Bean
	CustomStompSubProtocolErrorHandler customStompSubProtocolErrorHandler() {
		return new CustomStompSubProtocolErrorHandler();
	}

}