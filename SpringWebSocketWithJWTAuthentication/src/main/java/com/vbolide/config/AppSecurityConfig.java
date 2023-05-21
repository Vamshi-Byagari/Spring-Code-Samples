package com.vbolide.config;

import com.vbolide.config.jwt.JWTAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EnableMethodSecurity
@Configuration
public class AppSecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new CustomUserDetailsService();
	}

	@Bean
	JWTAuthenticationFilter jwtAuthenticationFilter() {
		return new JWTAuthenticationFilter();
	}


	@Value("${permitted.endpoint.antmatchers}")
	private String[] permittedEndPoints;

	@Value("${protected.endpoint.antmatchers}")
	private String[] protectedEndpoints;

	@Value("${permitted.cors.endpoint.pattern}")
	private String permittedCorsEndpointPattern;

	@Value("${permitted.cors.headers}")
	private String[] permittedCorsHeaders;


	@PostConstruct
	private void checkingValues() {
		System.out.printf(
			"SECURITY PROPERTIES :: permittedEndPoints: %s, protectedEndpoints: %s, permittedCorsEndpointPattern: %s, permittedCorsHeaders: %s%n",
			Arrays.toString(permittedEndPoints), Arrays.toString(protectedEndpoints), permittedCorsEndpointPattern, Arrays.toString(permittedCorsHeaders)
		);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity.cors()
			.and()
				.csrf().disable()
				.exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
			.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
				.authorizeRequests()
					.antMatchers(permittedEndPoints)
						.permitAll()
					.antMatchers(protectedEndpoints)
						.authenticated()
					.anyRequest()
						.permitAll()
			.and()
				.userDetailsService(userDetailsService())
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return httpSecurity.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		List<String> list = Collections.singletonList("*");

		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedHeaders(list);
		corsConfiguration.setAllowedMethods(list);
		corsConfiguration.setAllowedOrigins(list);
		corsConfiguration.setExposedHeaders(Arrays.asList(permittedCorsHeaders));

		UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
		urlBasedCorsConfigurationSource.registerCorsConfiguration(permittedCorsEndpointPattern, corsConfiguration);

		return urlBasedCorsConfigurationSource;
	}

}