package com.vbolide.controller;

import com.vbolide.model.CustomUserDetails;
import com.vbolide.model.Message;
import com.vbolide.service.PrincipalService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class AppWebSocketController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private final PrincipalService principalService;

	public AppWebSocketController(SimpMessagingTemplate simpMessagingTemplate, PrincipalService principalService){
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.principalService = principalService;
	}


	/** IN GENERAL `/topic` IS USED TO PUBLISH MESSAGES TO ALL THE CONNECTED USERS **/
	//after the successful STOMP CONNECT command, client subscribes to `/topic/updates` to receive any message published to that destination.
	//for publish messages to `/topic/updates`, client (or admin) has to send STOMP SEND command to `/app/broadcast`.

	@MessageMapping("/broadcast") //the message mapping value is not fixed (can use any string value)
	@SendTo("/topic/updates")
	public String broadcast(String message, Principal principal){
		//when authentication is done at STOMP CONNECT command we can directly use @AuthenticationPrincipal as method argument
		//for e.g @AuthenticationPrincipal CustomUserDetails customUserDetails

		//To retrieve CustomUserDetails from the Principal, if required.
		CustomUserDetails customUserDetails = principalService.getCustomUserDetails(principal);
		System.out.printf("customUserDetails: %s%n", customUserDetails);

		return message;
	}


	/** IN GENERAL `/queue` IS USED TO SEND MESSAGES FROM AND TO BETWEEN TWO CONNECTED USERS **/
	//Here `/app/chat` used to send one-to-one messages.
	//When Client subscribed to `/user/queue` (for receiving the messages targeted to that client).
	//1. a destination is created with `/user/queue-${user-session-id}`
	//2. when simpMessagingTemplate used to send messages it finds the session id associated to the username(Principal#getName())
	//2.1 for e.g Principal#getName() yields `vbolide@mail.com` whose session id is `ff87f338-753c-dca3-3f3c-ffc84d3aa816-4` the resultant destination will be `/user/queue-ff87f338-753c-dca3-3f3c-ffc84d3aa816-4`

	@MessageMapping("/chat") //the message mapping value is not fixed (can use any string value)
	public void chat(Message message) {
		simpMessagingTemplate.convertAndSendToUser(message.getRecipient(), "/queue", message.getValue());
	}



}