package com.vbolide.controller;

import com.vbolide.config.exception.APIError;
import com.vbolide.config.exception.APIErrorException;
import com.vbolide.config.jwt.JWTService;
import com.vbolide.model.LoginWrapper;
import com.vbolide.model.User;
import com.vbolide.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequestMapping("/api")
@RestController
public class LoginController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTService jwtService;


	public LoginController(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}


	@PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody User user) {
		if(userRepository.isUsernameAvailable(user.getEmail())) {
			try {
				user.setPassword(passwordEncoder.encode(user.getPassword()));
				user.setEncId(UUID.randomUUID().toString().replace("-", ""));
				user.setRoles("ROLE_USER");
				long userId = userRepository.insertUser(user);
				if(userId > 0) {
					user.setId(userId);
					return ResponseEntity.ok(Collections.singletonMap("jwt", jwtService.createJWT(user)));
				}else {
					throw new RuntimeException("userId is 0");
				}
			}catch (Exception e) {
				throw new RuntimeException(e.getLocalizedMessage());
			}
		}else {
			APIError apiError = new APIError(HttpStatus.BAD_REQUEST.value(), "validation errors");
			apiError.addValidationError("email", Collections.singletonList("email is already registered"));
			throw new APIErrorException(apiError);
		}
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginWrapper loginWrapper) {

		User user = userRepository.getUserWithEmail(loginWrapper.getEmail());
		if(Objects.isNull(user)) {
			APIError apiError = new APIError(HttpStatus.BAD_REQUEST.value(), "validation errors");
			apiError.addValidationError("email", Collections.singletonList("email doesn't exists"));
			throw new APIErrorException(apiError);
		}

		if(!passwordEncoder.matches(loginWrapper.getPassword(), user.getPassword())) {
			APIError apiError = new APIError(HttpStatus.BAD_REQUEST.value(), "validation errors");
			apiError.addValidationError("password", Collections.singletonList("invalid password"));
			throw new APIErrorException(apiError);
		}

		return ResponseEntity.ok(Collections.singletonMap("jwt", jwtService.createJWT(user)));
	}

}