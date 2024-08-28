package eu.solven.kumite.account.login;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class KumitePrivateController {

	@GetMapping("/private")
	public String privateEndpoint() {
		return "This is a private endpoint";
	}

	@GetMapping("/private/user")
	public Mono<String> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
		return oauth2User.map(OAuth2User::getName).map(name -> String.format("Hi, %s", name));
	}

}