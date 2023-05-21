package com.vbolide.config.websocket;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.vbolide.config.jwt.JWTService;
import com.vbolide.model.CustomUserDetails;

public class CustomHandshakeHandler extends DefaultHandshakeHandler{

	@Autowired
	private JWTService jwtService;

	@Autowired
	private ApplicationContext context;


	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

		String jwt = (String)attributes.get(HttpHeaders.AUTHORIZATION);
		System.out.printf("%s::%s: jwt: %s%n", this.getClass().getSimpleName(), "determineUser", jwt);
		if(Objects.isNull(jwt) || jwt.trim().isEmpty()) {
			return super.determineUser(request, wsHandler, attributes);
		}

		try {
			UserDetails userDetails = this.context.getBean(CustomUserDetails.class, jwtService.getUser(jwt));
			return new PreAuthenticatedAuthenticationToken(userDetails, "", userDetails.getAuthorities());
		}catch (Exception e) {
			return super.determineUser(request, wsHandler, attributes);
		}
	}

}