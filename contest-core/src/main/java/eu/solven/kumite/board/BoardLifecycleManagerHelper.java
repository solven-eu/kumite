package eu.solven.kumite.board;

import java.util.UUID;
import java.util.random.RandomGenerator;

import org.greenrobot.eventbus.EventBus;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class BoardLifecycleManagerHelper {

	// final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;
	final BoardsRegistry boardRegistry;

	final ContestPlayersRegistry contestPlayersRegistry;

	final EventBus eventBus;

	final RandomGenerator randomGenerator;

	public UUID doGameOver(UUID contestId, boolean force) {
		UUID boardStateId = boardRegistry.registerGameover(contestId, force);

		// The following will not change the latest boardStateId
		contestPlayersRegistry.gameover(contestId);

		return boardStateId;
	}

	public boolean isGameover(UUID contestId) {
		Contest contest = contestsRegistry.getContest(contestId);
		return getBoardRegistry().hasGameover(contest.getGame(), contestId).isGameOver();
	}

}
