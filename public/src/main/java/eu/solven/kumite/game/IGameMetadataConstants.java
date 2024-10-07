package eu.solven.kumite.game;

import java.util.UUID;

public interface IGameMetadataConstants {
	// Used to refer to something not existing. This is used for internal use (like race-conditions, as a placeholder
	// not to manipulate null). It should never be returned through API.
	UUID EMPTY = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

	// An optimization game consists in proposing the best solution to a given problem. They can be played independently
	// by any players.
	String TAG_OPTIMIZATION = "optimization";

	// Many games are `1v1` as they oppose 2 players on a given board.
	String TAG_1V1 = "1v1";

	// https://en.wikipedia.org/wiki/Perfect_information
	// https://www.reddit.com/r/boardgames/comments/bdi78u/what_are_some_simple_games_with_no_hidden/
	String TAG_PERFECT_INFORMATION = "perfect_information";

	/**
	 * A turn-based game is played at the pace of player moved. Players generally plays ones after the others
	 */
	String TAG_TURNBASED = "turn-based";

	/**
	 * A real-time game has its own pace. The game moves on even if players do not send move.
	 */
	String TAG_REALTIME = "real-time";

	/**
	 * A game which is played by a single player
	 */
	String TAG_SOLO = "solo";
}
