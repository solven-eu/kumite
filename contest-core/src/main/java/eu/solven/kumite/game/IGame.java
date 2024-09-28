package eu.solven.kumite.game;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;

public interface IGame {
	GameMetadata getGameMetadata();

	/**
	 * Default implementation returns true as many games may have only board-dependant rules
	 * 
	 * @param move
	 * @return true if this move is valid independently of the board state.
	 */
	// This should not return true by default, as no game would accept any IKumiteMove
	default boolean isValidMove(IKumiteMove move) {
		return true;
	}

	IKumiteBoard generateInitialBoard(RandomGenerator random);

	/**
	 * 
	 * @param contest
	 * @param player
	 * @return true if given player can join this game.
	 */
	default boolean canAcceptPlayer(Contest contest, KumitePlayer player) {
		if (!contest.isAcceptingPlayers()) {
			return false;
		} else if (contest.hasPlayerId(player.getPlayerId())) {
			// This player is already registered in given contest: most game accept each player at most once
			return false;
		}
		return true;
	}

	IKumiteMove parseRawMove(Map<String, ?> rawMove);

	IKumiteBoard parseRawBoard(Map<String, ?> rawBoard);

	default Leaderboard makeLeaderboard(IKumiteBoard board) {
		return Leaderboard.empty();
	}

	/**
	 * 
	 * @param boardView
	 * @param playerId
	 * @return a indicative {@link List} of valid moves for given {@link KumitePlayer} on given {@link IKumiteBoardView}
	 */
	default Map<String, IKumiteMove> exampleMoves(RandomGenerator randomGenerator,
			IKumiteBoardView boardView,
			UUID playerId) {
		return Collections.emptyMap();
	}

	IHasGameover makeDynamicGameover(IHasBoard hasBoard);

}
