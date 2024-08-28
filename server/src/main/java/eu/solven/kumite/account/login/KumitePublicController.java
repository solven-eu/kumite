package eu.solven.kumite.account.login;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.StreamSupport;

import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class KumitePublicController {
	final InMemoryReactiveClientRegistrationRepository clientRegistrationRepository;

	@GetMapping("/public")
	public String publicEndpoint() {
		return "This is a public endpoint";
	}

	@GetMapping("/login/providers")
	public Map<String, ?> loginProviders() {
		Map<String, Object> registrationIdToDetails = new TreeMap<>();

		StreamSupport.stream(clientRegistrationRepository.spliterator(), false)
				.filter(registration -> AuthorizationGrantType.AUTHORIZATION_CODE
						.equals(registration.getAuthorizationGrantType()))
				.forEach(r -> {
					// Typically 'github' or 'google'
					String registrationId = r.getRegistrationId();
					String loginUrl = "/oauth2/authorization/%s".formatted(registrationId);
					registrationIdToDetails.put(registrationId, Map.of("login_url", loginUrl));
				});

		return registrationIdToDetails;
	}

}