package eu.solven.kumite.contest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest.ContestBuilder;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ContestsRegistry {

	@NonNull
	final GamesRegistry gamesRegistry;

	// @NonNull
	// final LiveContestsRegistry liveContestsManager;

	@NonNull
	final ContestPlayersRegistry contestPlayersRegistry;

	@NonNull
	final BoardsRegistry boardsRegistry;

	@NonNull
	final IUuidGenerator uuidGenerator;

	Map<UUID, ContestCreationMetadata> uuidToContests = new ConcurrentHashMap<>();

	protected ContestCreationMetadata registerContest(UUID contestId, ContestCreationMetadata contest) {
		if (contestId == null) {
			throw new IllegalArgumentException("Missing contestId: " + contest);
		}

		ContestCreationMetadata alreadyIn = uuidToContests.putIfAbsent(contestId, contest);
		if (alreadyIn != null) {
			throw new IllegalArgumentException("contestId already registered: " + contest);
		}

		return contest;
	}

	public Contest registerContest(IGame game, ContestCreationMetadata constantMetadata, IKumiteBoard board) {
		UUID contestId = uuidGenerator.randomUUID();
		registerContest(contestId, constantMetadata);
		boardsRegistry.registerBoard(contestId, board);

		Contest contest = getContest(contestId);

		if (contest.isGameOver()) {
			// There is a small chance of the game turning before between `registerBoard` and now
			// (e.g. if the game has a very small timeout)
			throw new IllegalArgumentException("When registered, a contest has not to be over");
		}
		// else {
		// liveContestsManager.registerContestLive(contestId);
		// }

		return contest;
	}

	// public void registerGameOver(UUID contestId) {
	// liveContestsManager.registerContestOver(contestId);
	// }

	public Contest getContest(UUID contestId) {
		ContestCreationMetadata contestConstantMetadata = uuidToContests.get(contestId);
		if (contestConstantMetadata == null) {
			throw new IllegalArgumentException("No contest registered for id=" + contestId);
		}
		IHasBoard hasBoard = boardsRegistry.makeDynamicBoardHolder(contestId);
		IHasPlayers hasPlayers = contestPlayersRegistry.makeDynamicHasPlayers(contestId);

		UUID gameId = contestConstantMetadata.getGameId();

		IGame game = gamesRegistry.getGame(gameId);
		ContestBuilder contestBuilder = Contest.builder()
				.contestId(contestId)
				.game(game)
				.constantMetadata(contestConstantMetadata)
				.board(hasBoard)
				.players(hasPlayers)
				.gameover(game.makeDynamicGameover(hasBoard));

		return contestBuilder.build();
	}

	public List<Contest> searchContests(ContestSearchParameters search) {
		Stream<Map.Entry<UUID, ContestCreationMetadata>> contestStream;

		if (search.getContestId().isPresent()) {
			UUID uuid = search.getContestId().get();
			contestStream = Optional.ofNullable(uuidToContests.get(uuid)).map(c -> Map.entry(uuid, c)).stream();
		} else {
			contestStream = uuidToContests.entrySet().stream();
		}

		Stream<Contest> metaStream = contestStream.map(c -> getContest(c.getKey()));

		if (search.getGameId().isPresent()) {
			metaStream = metaStream.filter(c -> c.getGameMetadata().getGameId().equals(search.getGameId().get()));
		}

		if (search.isGameOver()) {
			metaStream = metaStream.filter(c -> c.isGameOver());
		}

		if (search.isAcceptPlayers()) {
			metaStream = metaStream.filter(c -> c.isAcceptingPlayers());
		}

		if (search.isRequirePlayers()) {
			metaStream = metaStream.filter(c -> c.isRequiringPlayers());
		}

		return metaStream.collect(Collectors.toList());
	}
}
