package com.vbolide.config.websocket;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.socket.CloseStatus;

import com.vbolide.config.jwt.JWTService;
import com.vbolide.config.websocket.exception.AuthenticationFailureException;
import com.vbolide.model.CustomUserDetails;

public class CustomClientInboundChannelInterceptor implements ChannelInterceptor {

	@Autowired
	private JWTService jwtService;

	@Autowired
	private ApplicationContext context;


	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		System.out.printf("%s::%s headers: %s%n", this.getClass().getSimpleName(), "preSend", message.getHeaders().size());
		StompHeaderAccessor stompHeaderAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if(!Objects.isNull(stompHeaderAccessor) && StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
			String jwt = stompHeaderAccessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
			System.out.printf("%s::%s jwt: %s%n", this.getClass().getSimpleName(), "preSend", jwt);
			if(Objects.isNull(jwt) || jwt.trim().isEmpty()) {
				throw new AuthenticationFailureException(CloseStatus.POLICY_VIOLATION.getCode(), "unauthorized");
			}

			UserDetails userDetails = this.context.getBean(CustomUserDetails.class, jwtService.getUser(jwt));
			PreAuthenticatedAuthenticationToken authenticationToken = new PreAuthenticatedAuthenticationToken(userDetails, "", userDetails.getAuthorities());
			stompHeaderAccessor.setUser(authenticationToken);
		}
		return ChannelInterceptor.super.preSend(message, channel);
	}

}