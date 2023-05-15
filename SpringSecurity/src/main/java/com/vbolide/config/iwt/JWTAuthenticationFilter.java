package com.vbolide.config.iwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbolide.config.exception.APIError;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

public class JWTAuthenticationFilter extends OncePerRequestFilter{

	@Autowired
	private JWTService jwtService;

	@Autowired
	private UserDetailsService userDetailsService;


	@Override
	protected void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
	) throws ServletException, IOException {

		System.out.printf("JWTAuthenticationFilter::doFilterInternal path: %s%n", request.getRequestURI());

		String authorizationHeader = request.getHeader("Authorization");
		if(authorizationHeader != null && !"".equals(authorizationHeader.trim())) {
			if(authorizationHeader.startsWith("Bearer ")) {
				String jwt = authorizationHeader.substring(7);
				if(jwt != null && !"".equals(jwt.trim())) {
					try {
						boolean isJWTExpired = jwtService.isJWTExpired(jwt);
						if(isJWTExpired) {
							invalid(response, HttpStatus.UNAUTHORIZED, "JWT is expired");
						}else {
							String username = jwtService.getUsernameFromJWT(jwt);
							if(username == null || "".equals(username.trim())) {
								invalid(response, HttpStatus.UNAUTHORIZED, "JWT is malformed and unsupported");
							}else {
								UserDetails userDetails = userDetailsService.loadUserByUsername(username);

								PreAuthenticatedAuthenticationToken authenticationToken 
									= new PreAuthenticatedAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
								authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

								SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
								securityContext.setAuthentication(authenticationToken);
								SecurityContextHolder.setContext(securityContext);

								filterChain.doFilter(request, response);
							}
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
				}else {
					invalid(response, HttpStatus.UNAUTHORIZED, "JWT is invlaid");
				}
			}else {
				filterChain.doFilter(request, response);
			}
		}else {
			filterChain.doFilter(request, response);
		}
	}

	private void invalid(HttpServletResponse response, HttpStatus status, String message) throws JsonProcessingException, IOException {
		response.setStatus(status.value());
		response.getWriter().write(new ObjectMapper().writeValueAsString(new APIError(status.value(), message)));
	}

}