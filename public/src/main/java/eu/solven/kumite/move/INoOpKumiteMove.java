package eu.solven.kumite.move;

/**
 * Marker interface for moves that are not real moves. These should not be submitted to the board (and are no-op if they
 * were sent).
 * 
 * @author Benoit Lacelle
 */
public interface INoOpKumiteMove extends IKumiteMove {
}
