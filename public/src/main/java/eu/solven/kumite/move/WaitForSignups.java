package eu.solven.kumite.move;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = "wait", allowGetters = true)
public class WaitForSignups implements IKumiteMove {

	// @JsonProperty(access = JsonProperty.Access.READ_ONLY)
	final boolean wait = true;

	@Builder.Default
	int missingPlayers = 1;
}
