package eu.solven.kumite.game;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.leaderboard.LeaderBoard;
import eu.solven.kumite.player.IKumiteMove;
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
	default boolean canAcceptPlayer(ContestMetadata contest, KumitePlayer player) {
		if (!contest.isAcceptingPlayers()) {
			return false;
		} else if (contest.getPlayers()
				.stream()
				.map(p -> p.getPlayerId())
				.anyMatch(p -> p.equals(player.getPlayerId()))) {
			// This player is already registered in given contest: most game accept each player at most once
			return false;
		}
		return true;
	}

	IKumiteMove parseRawMove(Map<String, ?> rawMove);

	IKumiteBoard parseRawBoard(Map<String, ?> rawBoard);

	default LeaderBoard makeLeaderboard(IKumiteBoard board) {
		return LeaderBoard.empty();
	}

	/**
	 * 
	 * @param boardView
	 * @param playerId
	 * @return a indicative {@link List} of valid moves for given {@link KumitePlayer} on given {@link IKumiteBoardView}
	 */
	default Map<String, IKumiteMove> exampleMoves(IKumiteBoardView boardView, UUID playerId) {
		return Collections.emptyMap();
	}

}
