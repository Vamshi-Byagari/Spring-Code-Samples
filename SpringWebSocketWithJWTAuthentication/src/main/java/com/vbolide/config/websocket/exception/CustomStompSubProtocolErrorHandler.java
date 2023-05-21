package com.vbolide.config.websocket.exception;

import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.vbolide.config.websocket.SimpMessageTemplateWrapper;

public class CustomStompSubProtocolErrorHandler extends StompSubProtocolErrorHandler{

	@Autowired
	private ApplicationContext context;


	@Override
	protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor, byte[] errorPayload,
			Throwable cause, StompHeaderAccessor clientHeaderAccessor) {

		Throwable rootCause = ExceptionUtils.getRootCause(cause);
		cause = Objects.isNull(rootCause) ? cause : rootCause;

		if(rootCause instanceof AuthenticationFailureException) {
			AuthenticationFailureException ex = (AuthenticationFailureException) rootCause;
			return MessageBuilder.createMessage(ex.getMessage().getBytes(), errorHeaderAccessor.getMessageHeaders());
		}

		if(cause instanceof AccessDeniedException) {
			SimpMessageTemplateWrapper simpMessageTemplateWrapper = context.getBean(SimpMessageTemplateWrapper.class);
			simpMessageTemplateWrapper.getSimpMessagingTemplate()
				.convertAndSendToUser(
				clientHeaderAccessor.getUser().getName(), 
				"/queue", 
				context.getBean(WebSocketException.class, clientHeaderAccessor.getDestination(), cause.getMessage())
			);
			return null;
		}

		
		
		//handle other stomp protocol level exceptions

		return super.handleInternal(errorHeaderAccessor, errorPayload, cause, clientHeaderAccessor);
	}

}