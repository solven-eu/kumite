package eu.solven.kumite.app.controllers;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.login.KumiteTokenService;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.player.KumitePlayer;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/login/v1")
@AllArgsConstructor
public class KumiteLoginController {
	final InMemoryReactiveClientRegistrationRepository clientRegistrationRepository;

	final KumiteUsersRegistry usersRegistry;
	final Environment env;

	final KumiteTokenService kumiteTokenService;

	// Redirect to the UI route showing the User how to login
	// @GetMapping
	// public ResponseEntity<?> login() {
	// return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, "/login").build();
	// }

	@GetMapping("/providers")
	public Map<String, ?> loginProviders() {
		Map<String, Object> registrationIdToDetails = new TreeMap<>();

		StreamSupport.stream(clientRegistrationRepository.spliterator(), false)
				.filter(registration -> AuthorizationGrantType.AUTHORIZATION_CODE
						.equals(registration.getAuthorizationGrantType()))
				.forEach(r -> {
					// Typically 'github' or 'google'
					String registrationId = r.getRegistrationId();
					String loginUrl = "/oauth2/authorization/%s".formatted(registrationId);
					registrationIdToDetails.put(registrationId,
							Map.of("registration_id", registrationId, "login_url", loginUrl));
				});

		return Map.of("map", registrationIdToDetails, "list", registrationIdToDetails.values());
	}

	// @PreAuthorize("isAuthenticated()")
	@GetMapping("/user")
	public Mono<KumiteUser> user(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_DEFAULT_FAKE_USER))) {
			KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("fakeProviderId").sub("fakeSub").build();
			KumiteUserRaw raw = KumiteUserRaw.builder()
					.rawRaw(rawRaw)
					.username("fakeUsername")
					.email("fake@fake")
					.name("Fake Me")
					.build();
			KumiteUser fakeUser = KumiteUser.builder()
					.accountId(UUID.randomUUID())
					.playerId(KumitePlayer.FAKE_PLAYER_ID)
					.raw(raw)
					.build();
			return Mono.just(fakeUser);
		} else {
			return oauth2User.map(o -> {
				String sub = o.getAttribute("id").toString();
				KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("github").sub(sub).build();
				KumiteUser user = usersRegistry.getUser(rawRaw);

				return user;
			}).switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No user")));
		}
	}

	@GetMapping("/token")
	public Mono<Map<String, ?>> token(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
		return user(oauth2User).map(user -> kumiteTokenService.wrapInJwtToken(user));
	}

}