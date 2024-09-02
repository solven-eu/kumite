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

	boolean isValidMove(IKumiteMove move);

	IKumiteBoard generateInitialBoard(RandomGenerator random);

	boolean canAcceptPlayer(ContestMetadata contest, KumitePlayer player);

	IKumiteMove parseRawMove(Map<String, ?> rawMove);

	IKumiteBoard parseRawBoard(Map<String, ?> rawBoard);

	LeaderBoard makeLeaderboard(IKumiteBoard board);

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
