package eu.solven.kumite.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.webflux.LoginRouteButNotAuthenticatedException;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * This {@link Controller} is dedicated to `js` usage. It bridges with the API by providing `refresh_token`s.
 * 
 * @author Benoit Lacelle
 *
 */
@RestController
@RequestMapping("/api/login/v1")
@AllArgsConstructor
public class KumiteLoginController {
	public static final String PROVIDERID_GITHUB = "github";
	public static final String PROVIDERID_TEST = "test";

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

	/**
	 * 
	 * @param oauth2User
	 * @return a REDIRECT to the relevant route to be rendered as HTML, given current user authentication status.
	 */
	@GetMapping("/html")
	public ResponseEntity<?> loginpage(@AuthenticationPrincipal OAuth2User oauth2User) {
		String url;
		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKEUSER))) {
			url = "login?" + IKumiteSpringProfiles.P_FAKEUSER;
		} else if (oauth2User == null) {
			url = "login";
		} else {
			url = "login?success";
		}

		// Spring-OAuth2-Login returns FOUND in case current user is not authenticated: let's follow this choice is=n
		// this route dedicated in proper direction.
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build();
	}

	// @PreAuthorize("isAuthenticated()")
	@GetMapping("/user")
	public Mono<KumiteUser> user(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKEUSER))) {
			return Mono.just(usersRegistry.getUser(FakePlayerTokens.FAKE_ACCOUNT_ID));
		} else if (oauth2User == null) {
			// Happens if this route is called without authentication
			return Mono.error(() -> new LoginRouteButNotAuthenticatedException("Lack of OAuth2 user"));
		} else {
			return oauth2User.map(o -> {
				KumiteUser user = userFromOAuth2(o);

				return user;
			}).switchIfEmpty(Mono.error(() -> new LoginRouteButNotAuthenticatedException("No user")));
		}
	}

	private KumiteUser userFromOAuth2(OAuth2User o) {
		String providerId = guessProviderId(o);
		String sub = getSub(providerId, o);
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId(providerId).sub(sub).build();
		KumiteUser user = usersRegistry.getUser(rawRaw);
		return user;
	}

	private String getSub(String providerId, OAuth2User o) {
		if (PROVIDERID_GITHUB.equals(providerId)) {
			Object sub = o.getAttribute("id");
			if (sub == null) {
				throw new IllegalStateException("Invalid sub: " + sub);
			}
			return sub.toString();
		} else if (PROVIDERID_TEST.equals(providerId)) {
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
		if (PROVIDERID_TEST.equals(o.getAttribute("providerId"))) {
			return PROVIDERID_TEST;
		}
		return PROVIDERID_GITHUB;
	}

	@GetMapping("/oauth2/token")
	public Mono<?> token(@AuthenticationPrincipal Mono<OAuth2User> oauth2User,
			@RequestParam(name = "player_id", required = false) String rawPlayerId,
			@RequestParam(name = "refresh_token", defaultValue = "false") boolean requestRefreshToken) {
		return user(oauth2User).map(user -> {
			if (requestRefreshToken) {
				// TODO Restrict if `rawPlayerId` is provided.
				if (!StringUtils.isEmpty(rawPlayerId)) {
					throw new IllegalArgumentException("`player_id` is not hanlded yet for `refresh_token=true`");
				}
				List<KumitePlayer> players = playersRegistry.makeDynamicHasPlayers(user.getAccountId()).getPlayers();
				// Beware this would not allow playerIds generated after the refresh_token creation
				Set<UUID> playerIds = players.stream().map(KumitePlayer::getPlayerId).collect(Collectors.toSet());
				return kumiteTokenService.wrapInJwtRefreshToken(user, playerIds);
			} else {
				UUID playerId =
						KumiteHandlerHelper.optUuid(Optional.ofNullable(rawPlayerId)).orElse(user.getPlayerId());
				checkValidPlayerId(user, playerId);
				return kumiteTokenService.wrapInJwtAccessToken(user, playerId);
			}
		});
	}

	void checkValidPlayerId(KumiteUser user, UUID playerId) {
		UUID accountId = user.getAccountId();
		if (!playersRegistry.makeDynamicHasPlayers(accountId).hasPlayerId(playerId)) {
			throw new IllegalArgumentException("player_id=" + playerId + " is not managed by accountId=" + accountId);
		}
	}

	// It seems much easier to return the CSRF through a dedicated API, than through Cookies and Headers, as SpringBoot
	// seems to require advanced tweaking to get it populated automatically
	// https://docs.spring.io/spring-security/reference/reactive/exploits/csrf.html#webflux-csrf-configure-custom-repository
	@GetMapping("/csrf")
	public Mono<ResponseEntity<?>> csrf(ServerWebExchange exchange) {
		Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
		if (csrfToken == null) {
			throw new IllegalStateException("No csrfToken is available");
		}

		return csrfToken.map(csrf -> ResponseEntity.ok()
				.header(csrf.getHeaderName(), csrf.getToken())
				.body(Map.of("header", csrf.getHeaderName())));
	}

	/**
	 * @return the logout URL as a 2XX code, as 3XX can not be intercepted with Fetch API.
	 * @see https://github.com/whatwg/fetch/issues/601#issuecomment-502667208
	 */
	@GetMapping("/logout")
	public Map<String, String> loginpage() {
		return Map.of(HttpHeaders.LOCATION, "/html/login?logout");
	}

}