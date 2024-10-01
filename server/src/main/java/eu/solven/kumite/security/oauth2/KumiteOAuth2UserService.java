package eu.solven.kumite.security.oauth2;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserDetails.KumiteUserDetailsBuilder;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import graphql.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * This is responsible for loading {@link OAuth2User} from an {@link OAuth2UserRequest}, typically through the
 * `/userinfo` endpoint.
 * 
 * We takre advantage of this step to register in our cache the details of going through users, hence automatically
 * registering logging-in Users..
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KumiteOAuth2UserService extends DefaultReactiveOAuth2UserService {
	public static final String PROVIDERID_GITHUB = "github";
	public static final String PROVIDERID_GOOGLE = "google";

	@Deprecated
	public static final String PROVIDERID_TEST = IKumiteTestConstants.PROVIDERID_TEST;

	// private final AccountsStore accountsStore;
	final KumiteUsersRegistry usersRegistry;

	@Override
	@SneakyThrows(OAuth2AuthenticationException.class)
	public Mono<OAuth2User> loadUser(OAuth2UserRequest oAuth2UserRequest) {
		log.trace("Load user {}", oAuth2UserRequest);
		Mono<OAuth2User> userFromProvider = super.loadUser(oAuth2UserRequest);
		return processOAuth2User(oAuth2UserRequest, userFromProvider);
	}

	@VisibleForTesting
	public KumiteUser onKumiteUserRaw(KumiteUserPreRegister userPreRegister) {
		KumiteUser user = usersRegistry.registerOrUpdate(userPreRegister);
		return user;
	}

	private Mono<OAuth2User> processOAuth2User(OAuth2UserRequest oAuth2UserRequest,
			Mono<OAuth2User> userFromProviderMono) {
		return userFromProviderMono.map(userFromProvider -> {
			// The following comment can be used to register new unittest on new registration
			// new ObjectMapper().writeValueAsString(userFromProvider.getAttributes());

			KumiteUserRawRaw rawRaw = oauth2ToRawRaw(userFromProvider);

			String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
			if (!registrationId.equals(rawRaw.getProviderId())) {
				throw new IllegalStateException("Inconsistent providerId inference from OAuth2User");
			}

			String keyForPicture = switch (registrationId) {
			case PROVIDERID_GITHUB:
				yield "avatar_url";
			case PROVIDERID_GOOGLE:
				yield "picture";
			default:
				throw new IllegalArgumentException("Unexpected value: " + registrationId);
			};
			KumiteUserDetailsBuilder kumiteUserBuilder = KumiteUserDetails.builder()
					// .rawRaw(rawRaw)
					.username(userFromProvider.getAttributes().get("name").toString())
					.name(userFromProvider.getAttributes().get("name").toString())
					.email(userFromProvider.getAttributes().get("email").toString());

			Object rawPicture = userFromProvider.getAttributes().get(keyForPicture);
			if (rawPicture != null) {
				kumiteUserBuilder = kumiteUserBuilder.picture(URI.create(rawPicture.toString()));
			}
			KumiteUserDetails rawUser = kumiteUserBuilder.build();

			KumiteUserPreRegister userPreRegister =
					KumiteUserPreRegister.builder().rawRaw(rawRaw).details(rawUser).build();

			KumiteUser user = onKumiteUserRaw(userPreRegister);

			log.trace("User info is {}", user);
			return userFromProvider;
		});

	}

	public static KumiteUserRawRaw oauth2ToRawRaw(OAuth2User o) {
		String providerId = guessProviderId(o);
		String sub = getSub(providerId, o);
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId(providerId).sub(sub).build();
		return rawRaw;
	}

	private static String getSub(String providerId, OAuth2User o) {
		if (PROVIDERID_GITHUB.equals(providerId)) {
			Object id = o.getAttribute("id");
			if (id == null) {
				throw new IllegalStateException("Invalid id: " + id);
			}
			return id.toString();
		} else if (PROVIDERID_GOOGLE.equals(providerId)) {
			Object sub = o.getAttribute("sub");
			if (sub == null) {
				throw new IllegalStateException("Invalid sub: " + sub);
			}
			return sub.toString();
		} else if (PROVIDERID_TEST.equals(providerId)) {
			Object id = o.getAttribute("id");
			if (id == null) {
				throw new IllegalStateException("Invalid sub: " + id);
			}
			return id.toString();
		} else {
			throw new IllegalStateException("Not managed providerId: " + providerId);
		}
	}

	private static String guessProviderId(OAuth2User o) {
		if (isGoogle(o)) {
			return PROVIDERID_GOOGLE;
		} else if (PROVIDERID_TEST.equals(o.getAttribute("providerId"))) {
			return PROVIDERID_TEST;
		}
		return PROVIDERID_GITHUB;
	}

	private static boolean isGoogle(OAuth2User o) {
		if (o.getAuthorities()
				.contains(new SimpleGrantedAuthority("SCOPE_https://www.googleapis.com/auth/userinfo.email"))) {
			return true;
		}

		// TODO Unclear when we receive iss or not (Changed around introducing
		// SocialWebFluxSecurity.oidcReactiveOAuth2UserService)
		URL issuer = (URL) o.getAttribute("iss");
		if (issuer == null) {
			return false;
		}
		URI uri;
		try {
			uri = issuer.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Invalid iss: `%s`".formatted(issuer), e);
		}
		return "https://accounts.google.com".equals(uri.toASCIIString());
	}

}