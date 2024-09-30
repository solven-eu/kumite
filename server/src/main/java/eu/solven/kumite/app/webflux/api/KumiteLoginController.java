package eu.solven.kumite.app.webflux.api;

import java.util.LinkedHashMap;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.security.LoginRouteButNotAuthenticatedException;
import eu.solven.kumite.security.oauth2.KumiteOAuth2UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

					Map<String, String> details = new LinkedHashMap<>();

					details.put("type", "oauth2");
					details.put("registration_id", registrationId);
					details.put("login_url", loginUrl);

					if (KumiteOAuth2UserService.PROVIDERID_GOOGLE.equals(registrationId)) {
						// https://developers.google.com/identity/branding-guidelines?hl=fr
						details.put("button_img", "/ui/img/google-web_light_sq_ctn.svg");
					} else if (KumiteOAuth2UserService.PROVIDERID_GITHUB.equals(registrationId)) {
						// https://github.com/logos
						details.put("button_img", "/ui/img/GitHub_Logo.png");
					}

					registrationIdToDetails.put(registrationId, details);
				});

		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKEUSER))) {
			registrationIdToDetails.put(IKumiteSpringProfiles.P_FAKEUSER,
					Map.of("type", "basic", "registration_id", "BASIC", "login_url", "/html/login/basic"));
		}

		return Map.of("map", registrationIdToDetails, "list", registrationIdToDetails.values());
	}

	/**
	 * 
	 * @param oauth2User
	 * @return a REDIRECT to the relevant route to be rendered as HTML, given current user authentication status.
	 */
	@GetMapping("/html")
	public Mono<ResponseEntity<?>> loginpage() {
		// Spring-OAuth2-Login returns FOUND in case current user is not authenticated: let's follow this choice is=n
		// this route dedicated in proper direction.
		return userMayEmpty().map(user -> "login?success")
				.switchIfEmpty(Mono.just("login"))
				.map(url -> ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build());

	}

	// This API enables fetching the login status without getting a 401/generating a JS error/generating an exception.
	@GetMapping("/json")
	public Mono<? extends Map<String, ?>> loginStatus(@AuthenticationPrincipal OAuth2User oauth2User) {
		return userMayEmpty().map(user -> Map.of("login", 200)).switchIfEmpty(Mono.just(Map.of("login", 401)));
	}

	// BASIC login is available for fakeUser
	@PostMapping("/basic")
	public Mono<Map> basic() {
		if (!env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKEUSER))) {
			throw new LoginRouteButNotAuthenticatedException("No BASIC");
		}

		return user().map(user -> Map.of("Authentication",
				"BASIC",
				"username",
				user.getAccountId(),
				HttpHeaders.LOCATION,
				"/html/login?success"));
	}

	private KumiteUser userFromOAuth2(OAuth2User o) {
		KumiteUserRawRaw rawRaw = KumiteOAuth2UserService.oauth2ToRawRaw(o);
		KumiteUser user = usersRegistry.getUser(rawRaw);
		return user;
	}

	// Provides the User if available. Not error if no user.
	private Mono<KumiteUser> userMayEmpty() {
		return ReactiveSecurityContextHolder.getContext().map(sc -> {
			Authentication authentication = sc.getAuthentication();

			if (authentication instanceof UsernamePasswordAuthenticationToken usernameToken) {
				// This happens on BASIC auth (for fakePlayer)
				return userFromUsername(usernameToken);
			} else if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
				// This happens on OAuth2 auth (e.g. Github login)
				return userFromOAuth2(oauth2Token.getPrincipal());
			} else {
				throw new LoginRouteButNotAuthenticatedException("Lack of authentication");
			}
		});
	}

	@GetMapping("/user")
	public Mono<KumiteUser> user() {
		return userMayEmpty().switchIfEmpty(Mono.error(() -> new LoginRouteButNotAuthenticatedException("No user")));
	}

	@PostMapping("/user")
	public Mono<KumiteUser> user(ServerWebExchange exchange, @RequestBody KumiteUserUpdate rawUpdates) {
		return user().flatMap(user -> {
			KumiteUser updatedUser = user;

			if (rawUpdates.getCountryCode().isPresent()) {
				String countryCode = rawUpdates.getCountryCode().get();

				KumiteUserRaw updatedRaw = user.getRaw().setCountryCode(countryCode);
				updatedUser = usersRegistry.registerOrUpdate(updatedRaw);

				log.info("accountId={} has countryCode updated {} -> {}",
						user.getAccountId(),
						user.getRaw().getCountryCode(),
						countryCode);
			}

			// return the updated user
			return Mono.just(updatedUser);
		});
	}

	private KumiteUser userFromUsername(UsernamePasswordAuthenticationToken usernameToken) {
		UUID accountId = UUID.fromString(usernameToken.getName());
		KumiteUser user = usersRegistry.getUser(accountId);
		return user;
	}

	@GetMapping("/oauth2/token")
	public Mono<?> token(@RequestParam(name = "player_id", required = false) String rawPlayerId,
			@RequestParam(name = "refresh_token", defaultValue = "false") boolean requestRefreshToken) {
		return user().map(user -> {
			if (requestRefreshToken) {
				// TODO Restrict if `rawPlayerId` is provided.
				if (!StringUtils.isEmpty(rawPlayerId)) {
					throw new IllegalArgumentException("`player_id` is not hanlded yet for `refresh_token=true`");
				}
				List<KumitePlayer> players = playersRegistry.makeDynamicHasPlayers(user.getAccountId()).getPlayers();
				// Beware this would not allow playerIds generated after the refresh_token creation
				Set<UUID> playerIds = players.stream().map(KumitePlayer::getPlayerId).collect(Collectors.toSet());
				log.info("Generating an refresh_token for accountId={} playerIds={}", user.getAccountId(), playerIds);
				return kumiteTokenService.wrapInJwtRefreshToken(user, playerIds);
			} else {
				UUID playerId = KumiteHandlerHelper.optUuid(Optional.ofNullable(rawPlayerId), "player_id")
						.orElse(user.getPlayerId());
				checkValidPlayerId(user, playerId);
				log.info("Generating an access_token for accountId={} playerId={}", user.getAccountId(), playerId);
				return kumiteTokenService.wrapInJwtAccessToken(user, playerId);
			}
		});
	}

	void checkValidPlayerId(KumiteUser user, UUID playerId) {
		UUID accountId = user.getAccountId();
		if (!playersRegistry.makeDynamicHasPlayers(accountId).hasPlayerId(playerId)) {
			throw new IllegalArgumentException("playerId=" + playerId + " is not managed by accountId=" + accountId);
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
	public Map<String, String> logout() {
		return Map.of(HttpHeaders.LOCATION, "/html/login?logout");
	}

}