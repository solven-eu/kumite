package eu.solven.kumite.app.webflux.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class KumitePublicController {

	@GetMapping("/api/v1/public")
	public String publicEndpoint() {
		return "This is a public endpoint";
	}

	@GetMapping("/api/v1/private")
	public String privateEndpoint() {
		return "This is a private endpoint";
	}

}