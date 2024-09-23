package eu.solven.kumite.player;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.move.IKumiteMove;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class PlayerMovesHolder {
	// Relates to a Map<String, IKumiteMove>, but IKumiteMove would not enable deserialization due to type ambiguity
	@Singular
	Map<String, IKumiteMove> moves;

	public static PlayerRawMovesHolder snapshot(PlayerMovesHolder playerMoves) {
		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, Map<String, ?>> rawMoves = playerMoves.getMoves()
				.entrySet()
				.stream()
				.map(e -> Map.entry(e.getKey(), objectMapper.convertValue(e.getValue(), Map.class)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return PlayerRawMovesHolder.builder().moves(rawMoves).build();
	}
}
