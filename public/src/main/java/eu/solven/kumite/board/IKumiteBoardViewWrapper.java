package eu.solven.kumite.board;

import java.util.UUID;

public interface IKumiteBoardViewWrapper {
	/**
	 * The id of the board. It does not depends on the view, but it is updated on any operation changing the board
	 * (including time).
	 * 
	 * @return
	 */
	UUID getBoardStateId();

	IKumiteBoardView getView();

}
