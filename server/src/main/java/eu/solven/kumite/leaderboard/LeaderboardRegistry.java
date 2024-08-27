package eu.solven.kumite.leaderboard;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
public class LeaderboardRegistry {

	@Getter(AccessLevel.NONE)
	Map<UUID, LeaderBoard> contestToLeaderboard = new ConcurrentHashMap<>();

	void registerScore(UUID contestId, IPlayerScore playerScore) {
		contestToLeaderboard.computeIfAbsent(contestId, k -> LeaderBoard.builder().build()).registerScore(playerScore);
	}

	public LeaderBoardRaw searchLeaderboard(LeaderboardSearchParameters search) {
		LeaderBoard leaderboard = contestToLeaderboard.getOrDefault(search.getContestId(), LeaderBoard.empty());

		Map<UUID, IPlayerScore> playerToScore;

		playerToScore = leaderboard.getPlayerIdToPlayerScore();
		// if (search.getPlayerId().isPresent()) {
		// UUID playerId = search.getPlayerId().get();
		// if (playerToScore.containsKey(playerId)) {
		// playerToScore = Map.of(playerId, playerToScore.get(playerId));
		// } else {
		// playerToScore = Map.of();
		// }
		// }

		// int rank = search.getRank().orElse(1);
		//
		// gamesStore.ge
		//
		// Should we get the comparator from the contest/game?
		List<PlayerDoubleScore> playerScores = playerToScore.values()
				.stream()
				.map(ps -> (PlayerDoubleScore) ps)
				.sorted(Comparator.comparing(ps -> ps.getScore()))
				.collect(Collectors.toList());

		return LeaderBoardRaw.builder().playerScores(playerScores).build();
	}
}
