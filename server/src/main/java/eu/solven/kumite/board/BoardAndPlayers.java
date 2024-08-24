package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMove;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class BoardAndPlayers {
	@NonNull
	IGame game;

	@NonNull
	IKumiteBoard board;

	@Singular
	List<KumitePlayer> players;

	public void checkValidMove(PlayerMove playerMove) {
		UUID playerId = playerMove.getPlayerId();
		IKumiteMove move = playerMove.getMove();

		if (players.stream().noneMatch(p -> p.getPlayerId().equals(playerId))) {
			throw new IllegalArgumentException("playerId=" + playerId + " is not registered in contest=");
		} else if (!game.isValidMove(move)) {
			throw new IllegalArgumentException("move=" + move + " is invalid");
		}

		List<String> invalidMoveReasons = board.isValidMove(playerMove);
		if (!invalidMoveReasons.isEmpty()) {
			throw new IllegalArgumentException(
					"move=" + move + " by playerId=" + playerId + " is invalid: " + invalidMoveReasons);
		}
	}

	public void registerMove(PlayerMove playerMove) {
		board.registerMove(playerMove);
	}
}
