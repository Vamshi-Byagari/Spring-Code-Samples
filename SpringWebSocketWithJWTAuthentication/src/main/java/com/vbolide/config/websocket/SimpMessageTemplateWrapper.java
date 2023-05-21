package com.vbolide.config.websocket;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Getter
public class SimpMessageTemplateWrapper {

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

}