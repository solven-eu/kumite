package eu.solven.kumite.account.login;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRawRaw;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class KumitePrivateController {
	final KumiteUsersRegistry usersRegistry;

	@GetMapping("/private")
	public String privateEndpoint() {
		return "This is a private endpoint";
	}

	@GetMapping("/private/user")
	public Mono<KumiteUser> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
		return oauth2User.map(o -> {
			// System.out.println(o);
			KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("github").sub(o.getAttribute("id").toString()).build();
			KumiteUser user = usersRegistry.getUser(rawRaw);

			return user;
		});
	}

}