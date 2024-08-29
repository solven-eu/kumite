package eu.solven.kumite.account.login;

import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
// @PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class KumitePrivateController {
	final KumiteUsersRegistry usersRegistry;
	final Environment env;

	@GetMapping("/private")
	public String privateEndpoint() {
		return "This is a private endpoint";
	}

	@GetMapping("/private/user")
	public Mono<KumiteUser> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_DEFAULT_FAKE_USER))) {
			KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("fakeProviderId").sub("fakeSub").build();
			KumiteUserRaw raw = KumiteUserRaw.builder()
					.rawRaw(rawRaw)
					.username("fakeUsername")
					.email("fake@fake")
					.name("Fake Me")
					.build();
			KumiteUser fakeUser = KumiteUser.builder().accountId(UUID.randomUUID()).raw(raw).build();
			return Mono.just(fakeUser);
		}

		return oauth2User.map(o -> {
			String sub = o.getAttribute("id").toString();
			KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("github").sub(sub).build();
			KumiteUser user = usersRegistry.getUser(rawRaw);

			return user;
		});
	}

}