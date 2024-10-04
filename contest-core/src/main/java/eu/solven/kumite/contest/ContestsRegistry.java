package eu.solven.kumite.contest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.greenrobot.eventbus.EventBus;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest.ContestBuilder;
import eu.solven.kumite.contest.persistence.IContestsRepository;
import eu.solven.kumite.events.ContestIsCreated;
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

	@NonNull
	final ContestPlayersRegistry contestPlayersRegistry;

	@NonNull
	final BoardsRegistry boardsRegistry;

	@NonNull
	final IUuidGenerator uuidGenerator;

	@NonNull
	final IContestsRepository contestsRepository;

	final EventBus eventBus;

	protected Optional<ContestCreationMetadata> registerContest(UUID contestId, ContestCreationMetadata contest) {
		if (contestId == null) {
			throw new IllegalArgumentException("Missing contestId: " + contest);
		}

		Optional<ContestCreationMetadata> optAlreadyIn = contestsRepository.putIfAbsent(contestId, contest);
		if (optAlreadyIn.isPresent()) {
			log.warn("Trying to initialize multiple times board for contestId={}", contestId);
		} else {
			log.info("Registered contestId={} for gameid={}", contestId, contest.getGameId());
		}
		return optAlreadyIn;
	}

	public Contest registerContest(IGame game, ContestCreationMetadata constantMetadata, IKumiteBoard board) {
		UUID contestId = uuidGenerator.randomUUID();
		registerContest(contestId, constantMetadata);
		boardsRegistry.registerBoard(contestId, board);

		Contest contest = getContest(contestId);

		eventBus.post(ContestIsCreated.builder().contestId(contest.getContestId()).build());

		return contest;
	}

	// public void registerGameOver(UUID contestId) {
	// liveContestsManager.registerContestOver(contestId);
	// }

	public Contest getContest(UUID contestId) {
		ContestCreationMetadata contestConstantMetadata = contestsRepository.getById(contestId)
				.orElseThrow(() -> new IllegalArgumentException("No contest registered for id=" + contestId));
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
				.gameover(boardsRegistry.hasGameover(game, contestId));

		return contestBuilder.build();
	}

	public List<Contest> searchContests(ContestSearchParameters search) {
		Stream<Map.Entry<UUID, ContestCreationMetadata>> contestStream;

		if (search.getContestId().isPresent()) {
			UUID uuid = search.getContestId().get();
			contestStream = contestsRepository.getById(uuid).map(c -> Map.entry(uuid, c)).stream();
		} else {
			contestStream = contestsRepository.stream();
		}

		Stream<Contest> metaStream = contestStream.map(c -> {
			UUID contestId = c.getKey();
			try {
				return getContest(contestId);
			} catch (RuntimeException e) {
				// This may happen if the board expired a bit before the contest metadata
				log.warn("Issue loading contestId={}", contestId, e);
				return null;
			}
		})
				// Filer the contest having failed to load
				.filter(Objects::nonNull);

		if (search.getGameId().isPresent()) {
			metaStream = metaStream.filter(c -> c.getGameMetadata().getGameId().equals(search.getGameId().get()));
		}

		if (search.getGameOver() != null) {
			metaStream = metaStream.filter(c -> search.getGameOver().equals(c.isGameOver()));
		}

		if (search.getAcceptPlayers() != null) {
			metaStream = metaStream.filter(c -> search.getAcceptPlayers().equals(c.isAcceptingPlayers()));
		}

		if (search.getRequirePlayers() != null) {
			metaStream = metaStream.filter(c -> search.getRequirePlayers().equals(c.isRequiringPlayers()));
		}

		return metaStream.collect(Collectors.toList());
	}

	public void deleteContest(UUID contestId) {
		boardsRegistry.forceGameover(contestId);
		contestPlayersRegistry.forceGameover(contestId);
		// contestsRepository.archive(contestId);
	}
}
