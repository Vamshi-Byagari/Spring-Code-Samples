package com.vbolide.config.websocket;

import com.vbolide.config.jwt.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CustomHandshakeInterceptor implements HandshakeInterceptor{

	@Autowired
	private JWTService jwtService;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

		String query = request.getURI().getQuery();
		if(Objects.isNull(query)) {
			return false;
		}

		HashMap<String, String> queryParams = new HashMap<>();
		Stream.of(query.split("&")).map(s -> s.split("=")).forEach(arr -> queryParams.put(arr[0], arr[1]));

		String jwt = queryParams.get("token");
		System.out.printf("%s::%s token: %s%n", this.getClass().getSimpleName(), "beforeHandshake", jwt);
		if(Objects.isNull(jwt) || jwt.trim().isEmpty()) {
			return false;
		}

		try {
			boolean isJWTExpired = jwtService.isJWTExpired(jwt);
			if(isJWTExpired) {
				return false;
			}

			String username = jwtService.getUsernameFromJWT(jwt);
			if(Objects.isNull(username) || username.trim().isEmpty()) {
				return false;
			}

			attributes.put(HttpHeaders.AUTHORIZATION, jwt);
		}catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, 
			Exception exception) {
		
	}

}