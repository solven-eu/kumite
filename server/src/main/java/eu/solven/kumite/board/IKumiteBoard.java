package eu.solven.kumite.board;

import java.util.List;

import eu.solven.kumite.player.PlayerMove;

public interface IKumiteBoard {

	List<String> isValidMove(PlayerMove playerMove);

	void registerMove(PlayerMove playerMove);

}
