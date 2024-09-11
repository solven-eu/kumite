package eu.solven.kumite.player;

import java.util.Map;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PlayerRawMovesHolder {
	// Relates to a Map<String, IKumiteMove>, but IKumiteMove would not enable deserialization due to type ambiguity
	@Singular
	Map<String, Map<String, ?>> moves;
}
