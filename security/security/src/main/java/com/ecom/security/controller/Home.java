package com.ecom.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class Home {
	@GetMapping("/")	
	public String getHome() {
		//ClassPathResource classPathResource = new ClassPathResource("product.json");
		
		return "Home Page";
	}
	
	@GetMapping("/error")	
	public String getError() {
		//ClassPathResource classPathResource = new ClassPathResource("product.json");
		
		return "Error Page";
	}
}
