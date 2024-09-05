package eu.solven.kumite.game.opposition.tictactoe;

import eu.solven.kumite.player.IKumiteMove;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A move is the next position picked by a player. A position is represented by its number. The first row positions are
 * numbered left-to-right from 4 to 6. The second row positions are numbered left-to-right from 1 to 3. The third row
 * positions are numbered left-to-right from 7 to 9.
 * 
 * (We follow the convention in the Wikipedia article: https://en.wikipedia.org/wiki/Tic-tac-toe). Coders may prefer
 * 0-based, bit it make look simpler for young coders as TicTacToe is a simple game.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class TicTacToeMove implements IKumiteMove {
	// Has to be between 1 and 9 (included)
	int position;
}
