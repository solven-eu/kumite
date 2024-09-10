package eu.solven.kumite.game.optimization.tsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.player.PlayerMoveRaw;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = { "boardSvg", "moveSvg" }, allowGetters = true)
public class TSPProblem implements IKumiteBoardView {
	// The unordered set of cities waiting to be visited
	@Singular
	Set<TSPCity> cities;

	@Override
	public List<String> isValidMove(PlayerMoveRaw playerMove) {
		TSPSolution s = (TSPSolution) playerMove.getMove();

		List<String> invalidReasons = new ArrayList<>();

		if (getCities().size() != s.getCities().size()) {
			invalidReasons.add("Inconsistent number of cities");
		}

		Map<String, TSPCity> nameToCity = new HashMap<>();
		getCities().forEach(c -> nameToCity.put(c.getName(), c));

		Set<String> visitedCities = new HashSet<>();
		s.getCities().forEach(c -> visitedCities.add(c));

		if (!nameToCity.keySet().equals(visitedCities)) {
			invalidReasons.add("Inconsistent set of cities");
		}

		return invalidReasons;
	}

	// This property has to be sent to the UI, but there is no point in reading-it back
	// @JsonProperty(access = Access.READ_ONLY)
	@Override
	public String getBoardSvg() {
		return "KumiteTSPBoardState";
	}

	@Override
	public String getMoveSvg() {
		return "KumiteTSPBoardMove";
	}
}
