package eu.solven.kumite.oauth2;

public interface IKumiteOAuth2Constants {
	// https://connect2id.com/products/server/docs/api/token#url
	String KEY_OAUTH2_ISSUER = "kumite.oauth2.issuer-base-url";
	String KEY_JWT_SIGNINGKEY = "kumite.oauth2.signing-key";

	// Used to generate a signingKey on the fly. Useful for integrationTests
	String GENERATE = "GENERATE";
}
