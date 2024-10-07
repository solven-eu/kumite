package eu.solven.kumite.board;

import java.util.UUID;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.PlayerJoinRaw;

public interface IBoardLifecycleManager {

	/**
	 * 
	 * @param contest
	 * @param playerRegistrationRaw
	 * @return
	 */
	IKumiteBoardViewWrapper registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw);

	IKumiteBoardViewWrapper onPlayerMove(Contest contest, PlayerMoveRaw playerMove);

	/**
	 * This covers any game-over which is not as defined by the gameRules. It typically includes timeout and author
	 * decision.
	 * 
	 * @param contest
	 * @return the boardStateId
	 */
	UUID forceGameOver(Contest contest);

}
