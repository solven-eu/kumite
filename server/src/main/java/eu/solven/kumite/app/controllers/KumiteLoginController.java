package eu.solven.kumite.app.controllers;

import java.util.Map;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.account.login.KumiteTokenService;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.webflux.LoginRouteButNotAuthenticatedException;
import eu.solven.kumite.login.AccessTokenHolder;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/login/v1")
@AllArgsConstructor
public class KumiteLoginController {
	final InMemoryReactiveClientRegistrationRepository clientRegistrationRepository;

	final KumiteUsersRegistry usersRegistry;
	final IAccountPlayersRegistry playersRegistry;
	final Environment env;

	final KumiteTokenService kumiteTokenService;

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
		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKE_USER))) {

			KumiteUser fakeUser = FakePlayerTokens.fakeUser();
			return Mono.just(fakeUser);
		} else if (oauth2User == null) {
			// Happens if this route is called without authentication
			return Mono.error(() -> new LoginRouteButNotAuthenticatedException());
		} else {
			return oauth2User.map(o -> {
				String providerId = guessProviderId(o);
				String sub = getSub(providerId, o);
				KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId(providerId).sub(sub).build();
				KumiteUser user = usersRegistry.getUser(rawRaw);

				return user;
			}).switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No user")));
		}
	}

	private String getSub(String providerId, OAuth2User o) {
		if ("github".equals(providerId)) {
			Object sub = o.getAttribute("id");
			if (sub == null) {
				throw new IllegalStateException("Invalid sub: " + sub);
			}
			return sub.toString();
		} else {
			throw new IllegalStateException("Not managed providerId: " + providerId);
		}
	}

	private String guessProviderId(OAuth2User o) {
		if ("testProviderId".equals(o.getAttribute("providerId"))) {
			return "testProviderId";
		}
		return "github";
	}

	@GetMapping("/token")
	public Mono<AccessTokenHolder> token(@AuthenticationPrincipal Mono<OAuth2User> oauth2User,
			@RequestParam(name = "player_id", required = false) String rawPlayerId) {
		return user(oauth2User).map(user -> {
			UUID playerId = KumiteHandlerHelper.optUuid(Optional.ofNullable(rawPlayerId)).orElse(user.getPlayerId());

			checkValidPlayerId(user, playerId);

			return kumiteTokenService.wrapInJwtToken(user, playerId);
		});
	}

	void checkValidPlayerId(KumiteUser user, UUID playerId) {
		UUID accountId = user.getAccountId();
		if (!playersRegistry.makeDynamicHasPlayers(accountId).hasPlayerId(playerId)) {
			throw new IllegalArgumentException("player_id=" + playerId + " is not managed by accountId=" + accountId);
		}
	}

}