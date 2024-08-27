package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMove;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class BoardAndPlayers implements IHasPlayers {
	@NonNull
	IGame game;

	@NonNull
	IKumiteBoard board;

	@NonNull
	@Getter(AccessLevel.NONE)
	IHasPlayers hasPlayers;
	
	@Override
	public List<KumitePlayer> getPlayers() {
		return hasPlayers.getPlayers();
	}

	public void checkValidMove(PlayerMove playerMove) {
		UUID playerId = playerMove.getPlayerId();
		IKumiteMove move = playerMove.getMove();

		if (getPlayers().stream().noneMatch(p -> p.getPlayerId().equals(playerId))) {
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

	public void registerPlayer(KumitePlayer player) {
		// TODO Auto-generated method stub
		
	}
}
