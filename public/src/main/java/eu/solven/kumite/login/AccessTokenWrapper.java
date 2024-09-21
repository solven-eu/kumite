package eu.solven.kumite.login;

import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccessTokenWrapper implements ITokenHolder {
	String accessToken;
	UUID playerId;
	String tokenType;
	// https://datatracker.ietf.org/doc/html/rfc6749#section-4.2.2
	// in seconds
	long expiresIn;
}
