package com.vbolide.config.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.Getter;

@Getter
public class SimpMessageTemplateWrapper {

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

}