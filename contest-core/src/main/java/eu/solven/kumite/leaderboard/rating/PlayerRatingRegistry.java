package eu.solven.kumite.leaderboard.rating;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.persistence.IBoardRepository;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.Leaderboard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Managed ELO/rating impact from contests once the contest if gameover.
 * 
 * @author Benoit Lacelle
 *
 */
// https://www.microsoft.com/en-us/research/project/trueskill-ranking-system/
// https://en.wikipedia.org/wiki/Elo_rating_system
@Slf4j
@AllArgsConstructor
public class PlayerRatingRegistry {
	final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;
	final IBoardRepository boardRepository;

	final IWinLosRatingRepository winLosRatingRepository;

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onContestIsGameover(ContestIsGameover event) {
		UUID contestId = event.getContestId();

		Optional<IKumiteBoard> optBoard = boardRepository.getBoard(contestId);
		if (optBoard.isEmpty()) {
			log.info("{} is being skipped as the board has already been dropped");
			return;
		}

		IKumiteBoard board = optBoard.get();

		Contest contest = contestsRegistry.getContest(contestId);
		IGame game = contest.getGame();

		Leaderboard leaderboard = game.makeLeaderboard(board);

		List<Map.Entry<UUID, IPlayerScore>> orderedPlayerScore = leaderboard.getPlayerIdToPlayerScore()
				.entrySet()
				.stream()
				.sorted(Comparator.comparing(e -> e.getValue().asNumber().doubleValue()))
				.collect(Collectors.toList());

		UUID gameId = game.getGameMetadata().getGameId();
		int nbPlayers = orderedPlayerScore.size();

		if (contest.getPlayerIds().size() <= 1) {
			log.info("No rating impact as contestId={} was played by {} players", contestId);
			return;
		} else if (game.getGameMetadata().getTags().contains(IGameMetadataConstants.TAG_1V1)) {
			if (contest.getPlayerIds().size() != 2) {
				throw new IllegalStateException(
						"Game is `%s` but nbPlayers=%s".formatted(IGameMetadataConstants.TAG_1V1, nbPlayers));
			}

			if (orderedPlayerScore.get(0).getValue().equals(orderedPlayerScore.get(1).getValue())) {
				// this is a tie
				contest.getPlayerIds().forEach(playerId -> winLosRatingRepository.markTie(gameId, playerId));
			} else {
				// This is not a tie
				winLosRatingRepository.markWin(gameId, orderedPlayerScore.get(0).getKey());
				winLosRatingRepository.markLos(gameId, orderedPlayerScore.get(0).getKey());
			}
		} else {
			Map.Entry<UUID, IPlayerScore> previousEntry = null;
			int rank = 0;
			int nbSameRank = 0;
			for (Map.Entry<UUID, IPlayerScore> entry : orderedPlayerScore) {
				if (previousEntry == null) {
					// This is the top score
				} else {
					if (previousEntry.getValue().equals(entry.getValue())) {
						// Current player has same score as previous player: they have same rank
						nbSameRank++;
					} else {
						rank += nbSameRank;
						nbSameRank = 0;
					}
				}

				winLosRatingRepository.markRank(gameId, orderedPlayerScore.get(0).getKey(), rank, nbPlayers);

				previousEntry = entry;
			}
		}
	}
}
