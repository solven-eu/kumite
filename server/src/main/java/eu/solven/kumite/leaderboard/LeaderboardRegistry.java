package eu.solven.kumite.leaderboard;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LeaderboardRegistry {
	final BoardsRegistry boardRegistry;
	final ContestsRegistry contestsRegistry;
	final GamesRegistry gamesRegistry;

	@Getter(AccessLevel.NONE)
	Map<UUID, LeaderBoard> contestToLeaderboard = new ConcurrentHashMap<>();

	void registerScore(UUID contestId, IPlayerScore playerScore) {

		contestToLeaderboard.computeIfAbsent(contestId, k -> LeaderBoard.builder().build()).registerScore(playerScore);
	}

	public LeaderBoardRaw searchLeaderboard(LeaderboardSearchParameters search) {
		UUID contestId = search.getContestId();
		IKumiteBoard board = boardRegistry.makeDynamicBoardHolder(contestId).get();

		Contest contest = contestsRegistry.getContest(contestId);
		IGame game = gamesRegistry.getGame(contest.getGame().getGameMetadata().getGameId());

		LeaderBoard leaderboard = game.makeLeaderboard(board);

		Map<UUID, IPlayerScore> playerToScore = leaderboard.getPlayerIdToPlayerScore();

		// We may introduce a feature to return a subset of the leaderboard, for instance around some playerId score

		// Should we get the comparator from the contest/game?
		List<PlayerDoubleScore> playerScores = playerToScore.values()
				.stream()
				.map(ps -> (PlayerDoubleScore) ps)
				.sorted(Comparator.comparing(ps -> ps.getScore()))
				.collect(Collectors.toList());

		return LeaderBoardRaw.builder().playerScores(playerScores).build();
	}
}
