package eu.solven.kumite.game.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.LeaderBoard;
import eu.solven.kumite.player.PlayerMove;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class TSPBoard implements IKumiteBoard {
	// The unordered set of cities waiting to be visited
	@Singular
	Set<TSPCity> cities;

	Map<UUID, TSPSolution> playerToLatestSolution = new ConcurrentHashMap<>();

	@Override
	public List<String> isValidMove(PlayerMove playerMove) {
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

	@Override
	public void registerMove(PlayerMove playerMove) {
		TSPSolution tsmSolution = (TSPSolution) playerMove.getMove();
		playerToLatestSolution.put(playerMove.getPlayerId(), tsmSolution);
	}
}
