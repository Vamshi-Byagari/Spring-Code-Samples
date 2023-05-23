package com.vbolide.config;

import com.vbolide.model.CustomUserDetails;
import com.vbolide.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService{

	private static final Logger LOG = LoggerFactory.getLogger(CustomUserDetailsService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplicationContext context;
	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			return context.getBean(CustomUserDetails.class, userRepository.getUserWithEmail(username));
		}catch (Exception e) {
			LOG.error("Exception@{}:loadUserByUsername with username: {}", this.getClass().getSimpleName(), username);
		}
		throw new UsernameNotFoundException(String.format("no user with the % is exists", username));
	}

}