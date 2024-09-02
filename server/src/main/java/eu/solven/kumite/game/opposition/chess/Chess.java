package eu.solven.kumite.game.opposition.chess;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.leaderboard.LeaderBoard;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;

// https://peterellisjones.com/posts/generating-legal-chess-moves-efficiently/
// https://github.com/desht/chesspresso
// http://www.chesspresso.org/
public class Chess implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("151c322d-27c7-4b94-9e83-70cbfba1ce7f"))
			.title("Chess")
			.tag(IGameMetadataConstants.TAG_1V1)
			.tag(IGameMetadataConstants.TAG_PERFECT_INFORMATION)
			.minPlayers(2)
			.maxPlayers(2)
			.shortDescription(
					"Chess is an abstract strategy game that involves no hidden information and no elements of chance. It is played on a chessboard with 64 squares arranged in an 8×8 grid. The players, referred to as \"White\" and \"Black\", each control sixteen pieces: one king, one queen, two rooks, two bishops, two knights, and eight pawns. White moves first, followed by Black. The game is typically won by checkmating the opponent's king, i.e. threatening it with inescapable capture. There are several ways a game can end in a draw.")
			.reference(URI.create("https://en.wikipedia.org/wiki/Chess"))
			.build();

	@Override
	public GameMetadata getGameMetadata() {
		return gameMetadata;
	}

	@Override
	public boolean isValidMove(IKumiteMove move) {
		return false;
	}

	@Override
	public IKumiteBoard generateInitialBoard(RandomGenerator random) {
		return ChessBoard.builder().build();
	}

	@Override
	public boolean canAcceptPlayer(ContestMetadata contest, KumitePlayer player) {
		return !contest.isRequirePlayers();
	}

	@Override
	public IKumiteMove parseRawMove(Map<String, ?> rawMove) {
		return ChessMove.builder().move(rawMove.get("move").toString()).build();
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return ChessBoard.builder().build();
	}

	@Override
	public LeaderBoard makeLeaderboard(IKumiteBoard board) {
		return LeaderBoard.empty();
	}

}
