package eu.solven.kumite.game.snake;

public interface ISnakeConstants {
	String EMPTY = """
			________
			________
			________
			________
			________
			________
			________
			________
			""";

	int D0_ = 0;
	int D3_ = 3;
	int D6_ = 6;
	int D9_ = 9;

	char D0 = '^';
	char D3 = '>';
	char D6 = '⌄';
	char D9 = '<';

	// The fruit symbol
	char F = 'F';

	// width
	int W = 8;
}
