package eu.solven.kumite.account.login;

import java.net.URI;

import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRaw.KumiteUserRawBuilder;
import eu.solven.kumite.account.KumiteUserRawRaw;
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

	// private final AccountsStore accountsStore;
	private final KumiteUsersRegistry usersRegistry;

	@Override
	@SneakyThrows(OAuth2AuthenticationException.class)
	public Mono<OAuth2User> loadUser(OAuth2UserRequest oAuth2UserRequest) {
		log.trace("Load user {}", oAuth2UserRequest);
		Mono<OAuth2User> userFromProvider = super.loadUser(oAuth2UserRequest);
		return processOAuth2User(oAuth2UserRequest, userFromProvider);
	}

	@VisibleForTesting
	public KumiteUser onKumiteUserRaw(KumiteUserRaw rawUser) {
		KumiteUser user = usersRegistry.registerOrUpdate(rawUser);
		return user;
	}

	private Mono<OAuth2User> processOAuth2User(OAuth2UserRequest oAuth2UserRequest,
			Mono<OAuth2User> userFromProviderMono) {
		return userFromProviderMono.map(userFromProvider -> {
			// The following comment can be used to register new unittest on new registration
			// new ObjectMapper().writeValueAsString(userFromProvider.getAttributes());

			String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

			String keyForSub = switch (registrationId) {
			case "github":
				yield "id";
			default:
				throw new IllegalArgumentException("Unexpected value: " + registrationId);
			};

			KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder()
					.providerId(registrationId)
					.sub(userFromProvider.getAttributes().get(keyForSub).toString())
					.build();

			String keyForPicture = switch (registrationId) {
			case "github":
				yield "avatar_url";
			default:
				throw new IllegalArgumentException("Unexpected value: " + registrationId);
			};
			KumiteUserRawBuilder kumiteUserBuilder = KumiteUserRaw.builder()
					.rawRaw(rawRaw)
					.username(userFromProvider.getAttributes().get("name").toString())
					.name(userFromProvider.getAttributes().get("name").toString())
					.email(userFromProvider.getAttributes().get("email").toString());

			Object rawPicture = userFromProvider.getAttributes().get(keyForPicture);
			if (rawPicture != null) {
				kumiteUserBuilder = kumiteUserBuilder.picture(URI.create(rawPicture.toString()));
			}
			KumiteUserRaw rawUser = kumiteUserBuilder.build();

			KumiteUser user = onKumiteUserRaw(rawUser);

			log.trace("User info is {}", user);
			return userFromProvider;
		});

	}

}