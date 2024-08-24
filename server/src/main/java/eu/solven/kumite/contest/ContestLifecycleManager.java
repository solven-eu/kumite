package eu.solven.kumite.contest;

import java.util.concurrent.ScheduledExecutorService;

import eu.solven.kumite.board.BoardAndPlayers;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.player.PlayerMove;

public class ContestLifecycleManager {
	GamesStore gamesStore;
	ContestsStore contestsStore;

	ScheduledExecutorService ses;

	public void onContestRegistered(IContest contest) {
		// contest.

		// long msB
		//
		// ses.schedule(() -> {
		// checkGameOver();
		// }, 0, TimeUnit.MILLISECONDS);
	}

	public void onPlayerMove(Contest contest, PlayerMove playerMove) {
		// IGame game = gamesStore.getGame(contest.getGame().getUuid());

		BoardAndPlayers playerAndBoard = contest.getRefBoard();

		// TODO Beware race-conditions. Should implement an eventQueue per contest?
		try {
			playerAndBoard.checkValidMove(playerMove);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Issue on contest=" + contest, e);
		}

		playerAndBoard.registerMove(playerMove);
	}
}
