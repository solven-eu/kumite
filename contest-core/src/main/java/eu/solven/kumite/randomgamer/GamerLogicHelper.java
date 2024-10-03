package eu.solven.kumite.randomgamer;

import java.util.random.RandomGenerator;

import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Holds the components nedded for {@link AGamerLogic}
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
@Value
public class GamerLogicHelper {
	final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;
	final ContestPlayersRegistry contestPlayersRegistry;

	final BoardsRegistry boardsRegistry;
	final RandomGenerator randomGenerator;

	final BoardLifecycleManager boardLifecycleManager;
}
