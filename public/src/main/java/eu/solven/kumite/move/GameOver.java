package eu.solven.kumite.move;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = "gameOver", allowGetters = true)
public class GameOver implements IKumiteMove {

	// @JsonProperty(access = JsonProperty.Access.READ_ONLY)
	final boolean gameOver = true;
}
