package eu.solven.kumite.board;

import java.util.UUID;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsStore;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMove;

public class BoardLifecycleManager {
	GamesStore gamesStore;
	ContestsStore contestsStore;

	ContestPlayersRegistry contestPlayersRegistry;

	public void onPlayerRegistered(Contest contest, KumitePlayer newPlayer) {
		
	}

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
