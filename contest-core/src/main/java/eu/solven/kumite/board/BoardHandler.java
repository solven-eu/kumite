package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.webflux.api.KumiteHandlerHelper;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestDynamicMetadata;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerContestStatus;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class BoardHandler {
	@NonNull
	final GamesRegistry gamesRegistry;

	@NonNull
	final ContestsRegistry contestsRegistry;

	@NonNull
	final ContestPlayersRegistry contestPlayersRegistry;

	@NonNull
	final BoardsRegistry boardsRegistry;

	@NonNull
	final BoardLifecycleManager boardLifecycleManager;

	public Mono<ServerResponse> getBoard(ServerRequest request) {
		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		ContestSearchParameters search = ContestSearchParameters.searchContestId(contestId);
		List<Contest> contest = contestsRegistry.searchContests(search);
		if (contest.isEmpty()) {
			// https://stackoverflow.com/questions/5604816/whats-the-most-appropriate-http-status-code-for-an-item-not-found-error-page
			// We may want a specific exception + httpStatusCode
			throw new IllegalArgumentException("No contest for contestId=" + contestId);
		} else if (contest.size() >= 2) {
			throw new IllegalStateException("Multiple contests for contestId=" + contestId + " contests=" + contest);
		}

		Contest contestMetadata = contest.get(0);

		PlayerContestStatus playingPlayer = contestPlayersRegistry.getPlayingPlayer(playerId, contestMetadata);
		
		IKumiteBoard board = boardsRegistry.hasBoard(contestId).get();

		ContestView contestView = makeContestView(contestMetadata, playingPlayer, board);
		log.debug("Serving board for contestId={}", contestView.getContestId());

		return KumiteHandlerHelper.okAsJson(contestView);
	}

	private ContestView makeContestView(Contest contestMetadata,
			PlayerContestStatus playingPlayer,
			IKumiteBoard board) {
		UUID viewPlayerId;
		if (playingPlayer.isPlayerHasJoined()) {
			viewPlayerId = playingPlayer.getPlayerId();
		} else if (playingPlayer.isAccountIsViewing()) {
			viewPlayerId = KumitePlayer.AUDIENCE_PLAYER_ID;
		} else {
			viewPlayerId = KumitePlayer.PREVIEW_PLAYER_ID;
		}

		IKumiteBoardView boardView = board.asView(viewPlayerId);

		ContestDynamicMetadata dynamicMetadata = Contest.snapshot(contestMetadata).getDynamicMetadata();
		ContestView contestView = ContestView.fromView(boardView)
				.contestId(contestMetadata.getContestId())
				.playerStatus(playingPlayer)
				.dynamicMetadata(dynamicMetadata)

				.build();
		return contestView;
	}
}