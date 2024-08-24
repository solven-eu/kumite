package eu.solven.kumite.contest;

import java.util.UUID;

import eu.solven.kumite.game.IHasGame;
import eu.solven.kumite.player.IHasPlayers;

public interface IContest extends IHasGame, IHasPlayers {
	UUID getContestId();

	boolean isGameOver();

	default boolean isAcceptPlayers() {
		return getPlayers().size() < getGameMetadata().getMaxPlayers();
	}
}
