package com.vbolide.config.jwt;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbolide.config.exception.APIError;
import com.vbolide.model.CustomUserDetails;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

public class JWTAuthenticationFilter extends OncePerRequestFilter{

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String AUTHORIZATION_TYPE = "Bearer" ;
	
	@Autowired
	private JWTService jwtService;

	@Autowired
	private ApplicationContext context;


	@Override
	protected void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
	) throws ServletException, IOException {

		System.out.printf("JWTAuthenticationFilter::doFilterInternal path: %s%n", request.getRequestURI());

		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
		if(Objects.isNull(authorizationHeader) || authorizationHeader.trim().isEmpty() || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String jwt = authorizationHeader.substring(AUTHORIZATION_TYPE.length()).trim();
		if(jwt.trim().isEmpty()) {
			invalid(response, HttpStatus.UNAUTHORIZED, "JWT is invlaid");
			return;
		}

		try {
			boolean isJWTExpired = jwtService.isJWTExpired(jwt);
			if(isJWTExpired) {
				invalid(response, HttpStatus.UNAUTHORIZED, "JWT is expired");
			}else {
				String username = jwtService.getUsernameFromJWT(jwt);

				if(Objects.isNull(username) || username.trim().isEmpty()) {
					invalid(response, HttpStatus.UNAUTHORIZED, "JWT is malformed and unsupported");
					return;
				}

				//the purpose of jwt is to have the user authenticated w/o username and password.
				//as the claims already contains the information of user, we can make use of those and process the request.
				//if the attacker made changes to claims, then the jwt validation throws exception as it is not matched with the issued jwt.

				//User instance has values for fields that are available in jwt claims.
				//when a request requires the fields other than those, that request must query the database and get those prior to processing.
				UserDetails userDetails = loadUserFromToken(jwt);
				//UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				PreAuthenticatedAuthenticationToken authenticationToken 
					= new PreAuthenticatedAuthenticationToken(userDetails, "", userDetails.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
				securityContext.setAuthentication(authenticationToken);
				SecurityContextHolder.setContext(securityContext);

				filterChain.doFilter(request, response);
			}
		}catch (ExpiredJwtException e) {
			invalid(response, HttpStatus.UNAUTHORIZED, "JWT is expired");
		}catch (UnsupportedJwtException e) {
			invalid(response, HttpStatus.UNAUTHORIZED, "JWT is unsupported");
		}catch (MalformedJwtException e) {
			invalid(response, HttpStatus.UNAUTHORIZED, "JWT is malformed");
		}catch (SignatureException e) {
			invalid(response, HttpStatus.UNAUTHORIZED, "JWT is malformed and invalid");
		}catch (IllegalArgumentException e) {
			invalid(response, HttpStatus.BAD_REQUEST, "unknown error occured");
		}catch (UsernameNotFoundException e) {
			invalid(response, HttpStatus.UNAUTHORIZED, e.getMessage());
		}catch (Exception e) {
			invalid(response, HttpStatus.INTERNAL_SERVER_ERROR, "unknown error occured");
		}
	}

	private UserDetails loadUserFromToken(String jwt) {
		return this.context.getBean(CustomUserDetails.class, jwtService.getUser(jwt));
	}

	private void invalid(HttpServletResponse response, HttpStatus status, String message) throws JsonProcessingException, IOException {
		response.setStatus(status.value());
		response.getWriter().write(new ObjectMapper().writeValueAsString(new APIError(status.value(), message)));
	}

}