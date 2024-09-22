package eu.solven.kumite.app.webflux.api;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Greeting {
	String message;
}