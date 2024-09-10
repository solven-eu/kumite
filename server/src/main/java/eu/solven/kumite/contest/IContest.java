package eu.solven.kumite.contest;

import java.util.UUID;

import eu.solven.kumite.game.IHasGame;
import eu.solven.kumite.player.IHasPlayers;

public interface IContest extends IHasGame, IHasPlayers, IHasGameover {
	UUID getContestId();

	default boolean isRequiringPlayers() {
		return getPlayers().size() < getGameMetadata().getMinPlayers();
	}

	default boolean isAcceptingPlayers() {
		return getPlayers().size() < getGameMetadata().getMaxPlayers();
	}
}
