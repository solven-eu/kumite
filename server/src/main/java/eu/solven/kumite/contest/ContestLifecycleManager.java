package eu.solven.kumite.contest;

import java.util.UUID;

import eu.solven.kumite.board.BoardAndPlayers;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerMove;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ContestLifecycleManager {
	@NonNull
	GamesStore gamesStore;
	@NonNull
	ContestsStore contestsStore;

	@NonNull
	ContestPlayersRegistry contestPlayersRegistry;

	// ScheduledExecutorService ses;
	//
	// public void onContestRegistered(IContest contest) {
	// // contest.
	//
	// // long msB
	// //
	// // ses.schedule(() -> {
	// // checkGameOver();
	// // }, 0, TimeUnit.MILLISECONDS);
	// }

	public void onPlayerMove(Contest contest, PlayerMove playerMove) {
		UUID contestId = contest.getContestMetadata().getContestId();
		UUID playerId = playerMove.getPlayerId();

		if (!contestPlayersRegistry.isRegisteredPlayer(contestId, playerId)) {
			throw new IllegalArgumentException("playerId=" + playerId + " is not registered in contestId=" + contestId);
		}

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
