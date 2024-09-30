package eu.solven.kumite.account;

import java.net.URI;

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
public class KumiteUserRaw {
	@NonNull
	KumiteUserRawRaw rawRaw;

	@NonNull
	String username;

	String name;

	String email;

	URI picture;

	// https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes
	String countryCode;

	String school;
	String company;

	private KumiteUserRawBuilder preloadBuilder() {
		return KumiteUserRaw.builder()
				.rawRaw(rawRaw)
				.username(username)
				.name(name)
				.email(email)
				.picture(picture)
				.countryCode(countryCode)
				.school(school)
				.company(company);
	}

	public KumiteUserRaw setCountryCode(String countryCode) {
		return preloadBuilder().countryCode(countryCode).build();
	}

	public KumiteUserRaw setCompany(String company) {
		return preloadBuilder().company(company).build();
	}

	public KumiteUserRaw setSchool(String school) {
		return preloadBuilder().school(school).build();
	}
}
