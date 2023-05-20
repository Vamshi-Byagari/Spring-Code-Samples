package com.vbolide.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

	@GetMapping(value = "/test", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> test(){
		return ResponseEntity.ok("<html><head></head><body></body></html>");
	}

}