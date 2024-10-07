package eu.solven.kumite.websocket.stomp;

import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.board.IKumiteBoardViewWrapper;
import eu.solven.kumite.board.persistence.IBoardRepository;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.greeting.Greeting;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.PlayerJoinRaw;
import eu.solven.kumite.player.PlayerMovesHolder;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import lombok.AllArgsConstructor;

// https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-annotations.html
@Controller
@AllArgsConstructor
public class BoardWebsocketController {
	final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;
	final IBoardRepository boardRepository;
	final BoardLifecycleManager boardLifecycle;

	final RandomGenerator randomGenerator;

	@MessageMapping("/api/v1" + "/hello")
	@SendTo("/api/v1" + "/news")
	public Greeting hello() {
		return Greeting.builder().message("Hello, Spring!").build();
	}

	@MessageMapping("/api/v1" + "/board/{contestId}/player/{playerId}/view")
	@SendTo("/api/v1" + "/board")
	public IKumiteBoardView getBoard(@DestinationVariable("contestId") UUID contestId,
			@DestinationVariable("playerId") UUID playerId) {
		IKumiteBoardView view = boardRepository.getBoard(contestId).get().asView(playerId);

		return view;
	}

	@MessageMapping("/api/v1" + "/board/{contestId}/player/{playerId}/join")
	@SendTo("/api/v1" + "/board")
	public IKumiteBoardViewWrapper playerJoin(@DestinationVariable("contestId") UUID contestId,
			@DestinationVariable("playerId") UUID playerId) {
		Contest contest = contestsRegistry.getContest(contestId);

		IKumiteBoardViewWrapper view = boardLifecycle.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(contestId).playerId(contestId).build());

		return view;
	}

	@MessageMapping("/api/v1" + "/board/{contestId}/player/{playerId}/moves")
	@SendTo("/api/v1" + "/board")
	public PlayerRawMovesHolder listMoves(@DestinationVariable("contestId") UUID contestId,
			@DestinationVariable("playerId") UUID playerId) {
		Contest contest = contestsRegistry.getContest(contestId);

		IKumiteBoardView view = boardRepository.getBoard(contestId).get().asView(playerId);
		Map<String, IKumiteMove> moves = contest.getGame().exampleMoves(randomGenerator, view, playerId);

		PlayerMovesHolder movesHolder = PlayerMovesHolder.builder().moves(moves).build();

		return PlayerMovesHolder.snapshot(movesHolder);
	}

	@MessageMapping("/api/v1" + "/board/{contestId}/player/{playerId}/move")
	@SendTo("/api/v1" + "/board")
	public IKumiteBoardViewWrapper playMove(@DestinationVariable("contestId") UUID contestId,
			@DestinationVariable("playerId") UUID playerId,
			Map<String, ?> rawMove) {
		Contest contest = contestsRegistry.getContest(contestId);

		IKumiteMove move = contest.getGame().parseRawMove(rawMove);

		return boardLifecycle.onPlayerMove(contest, PlayerMoveRaw.builder().playerId(playerId).move(move).build());
	}

}