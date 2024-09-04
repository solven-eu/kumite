package eu.solven.kumite.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class KumitePublicController {

	@GetMapping("/api/public")
	public String publicEndpoint() {
		return "This is a public endpoint";
	}

	@GetMapping("/api/private")
	public String privateEndpoint() {
		return "This is a private endpoint";
	}

}