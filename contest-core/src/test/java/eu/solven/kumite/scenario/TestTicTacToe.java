package eu.solven.kumite.scenario;

import org.assertj.core.api.Assertions;

import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;

public class TestTicTacToe extends ATestTurnBased {

	@Override
	public IGame getGame() {
		return new TicTacToe();
	}

	@Override
	protected void checkOnNbMoves(int totalMoves) {
		// We need at least 5 moves to win (3 Xs and 2 Os)
		// Until 9 moves for a tie
		Assertions.assertThat(totalMoves).isBetween(5, 9);
	}

}
