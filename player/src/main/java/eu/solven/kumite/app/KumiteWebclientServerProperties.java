package eu.solven.kumite.app;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KumiteWebclientServerProperties {
	String baseUrl;
	String refreshToken;
}
