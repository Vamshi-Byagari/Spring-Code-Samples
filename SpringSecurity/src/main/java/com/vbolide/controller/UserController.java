package com.vbolide.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vbolide.config.annotaions.access.UserAccess;
import com.vbolide.model.CustomUserDetails;
import com.vbolide.model.User;
import com.vbolide.repository.UserRepository;

@UserAccess
@RestController
public class UserController {

	private final UserRepository userRepository;

	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/profile")
	public ResponseEntity<User> profile(@AuthenticationPrincipal CustomUserDetails customUserDetails){
		return ResponseEntity.ok(userRepository.getUser(customUserDetails.getUser().getEmail()));
	}

}