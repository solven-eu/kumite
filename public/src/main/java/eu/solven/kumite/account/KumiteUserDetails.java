package eu.solven.kumite.account;

import java.net.URI;

import eu.solven.kumite.account.internal.KumiteUser;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Like {@link KumiteUser} but without knowledge of the accountId.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class KumiteUserDetails {
	// @NonNull
	// KumiteUserRawRaw rawRaw;

	@NonNull
	String username;

	String name;

	String email;

	URI picture;

	// https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes
	String countryCode;

	String school;
	String company;

	private KumiteUserDetailsBuilder preloadBuilder() {
		return KumiteUserDetails.builder()
				// .rawRaw(rawRaw)
				.username(username)
				.name(name)
				.email(email)
				.picture(picture)
				.countryCode(countryCode)
				.school(school)
				.company(company);
	}

	public KumiteUserDetails setCountryCode(String countryCode) {
		return preloadBuilder().countryCode(countryCode).build();
	}

	public KumiteUserDetails setCompany(String company) {
		return preloadBuilder().company(company).build();
	}

	public KumiteUserDetails setSchool(String school) {
		return preloadBuilder().school(school).build();
	}
}
