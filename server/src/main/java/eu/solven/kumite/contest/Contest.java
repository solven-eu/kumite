package eu.solven.kumite.contest;

import java.util.List;
import java.util.UUID;

import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.PlayerMoveRaw;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Contest {
	@NonNull
	ContestMetadata contestMetadata;

	@NonNull
	IGame game;

	@NonNull
	IHasBoard board;

	@NonNull
	@Getter(AccessLevel.NONE)
	IHasPlayers hasPlayers;

	public void checkValidMove(PlayerMoveRaw playerMove) {
		UUID playerId = playerMove.getPlayerId();
		IKumiteMove move = playerMove.getMove();

		if (hasPlayers.getPlayers().stream().noneMatch(p -> p.getPlayerId().equals(playerId))) {
			throw new IllegalArgumentException("playerId=" + playerId + " is not registered");
		} else if (!game.isValidMove(move)) {
			throw new IllegalArgumentException("move=" + move + " is invalid");
		}

		List<String> invalidMoveReasons = board.get().isValidMove(playerMove);
		if (!invalidMoveReasons.isEmpty()) {
			throw new IllegalArgumentException(
					"move=" + move + " by playerId=" + playerId + " is invalid: " + invalidMoveReasons);
		}
	}
}
