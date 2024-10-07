package eu.solven.kumite.leaderboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LeaderboardRegistry {
	final BoardsRegistry boardRegistry;
	final ContestsRegistry contestsRegistry;
	final GamesRegistry gamesRegistry;

	public LeaderboardRaw searchLeaderboard(LeaderboardSearchParameters search) {
		UUID contestId = search.getContestId();
		IKumiteBoard board = boardRegistry.hasBoard(contestId).get();

		Contest contest = contestsRegistry.getContest(contestId);
		IGame game = gamesRegistry.getGame(contest.getGame().getGameMetadata().getGameId());

		Leaderboard leaderboard = game.makeLeaderboard(board);

		Map<UUID, IPlayerScore> playerToScore = leaderboard.getPlayerIdToPlayerScore();

		// We may introduce a feature to return a subset of the leaderboard, for instance around some playerId score

		// Should we get the comparator from the contest/game?
		List<IPlayerScore> playerScores = playerToScore.values()
				.stream()
				.sorted(Comparator.comparing(ps -> ps.asNumber().doubleValue()))
				.collect(Collectors.toList());

		List<PlayerScoreRaw> rawPlayerScores = new ArrayList<>(playerScores.size());

		for (int rank = 0; rank < playerScores.size(); rank++) {
			IPlayerScore playerScore = playerScores.get(rank);
			PlayerScoreRaw rawScore = PlayerScoreRaw.builder()
					.rank(rank)
					.playerId(playerScore.getPlayerId())
					.score(playerScore.asNumber())
					.build();
			rawPlayerScores.add(rawScore);
		}

		return LeaderboardRaw.builder().playerScores(rawPlayerScores).build();
	}
}
