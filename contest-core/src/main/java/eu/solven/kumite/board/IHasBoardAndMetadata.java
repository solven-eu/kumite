package eu.solven.kumite.board;

public interface IHasBoardAndMetadata {
	IKumiteBoard getBoard();

	BoardDynamicMetadata getMetadata();
}
