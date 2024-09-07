package eu.solven.kumite.player;

public interface IKumiteMove {
	/**
	 * 
	 * @return true if this move is just telling we need more players to join the contest
	 */
	// default boolean isWaitingForSignups() {
	// return false;
	// }

	/**
	 * 
	 * @return true if we could/should/have to wait for another player move
	 */
	// default boolean isWaitingForPlayerMove() {
	// return false;
	// }
}
