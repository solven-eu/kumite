package eu.solven.kumite.contest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
public class ContestsStore {
	LiveContestsManager liveContestsManager;

	Map<UUID, Contest> uuidToContests = new ConcurrentHashMap<>();

	public void registerContest(Contest contest) {
		UUID contestId = contest.getContestMetadata().getContestId();

		if (contestId == null) {
			throw new IllegalArgumentException("Missing contestId: " + contest);
		}

		Contest alreadyIn = uuidToContests.putIfAbsent(contestId, contest);
		if (alreadyIn != null) {
			throw new IllegalArgumentException("contestId already registered: " + contest);
		}

		if (contest.getContestMetadata().isGameOver()) {
			throw new IllegalArgumentException("When registered, a contest has not to be over");
		} else {
			liveContestsManager.registerContestLive(contestId);
		}
	}

	public void registerGameOver(UUID contestId) {
		liveContestsManager.registerContestOver(contestId);
	}

	public Contest getContest(UUID contestUuid) {
		Contest contest = uuidToContests.get(contestUuid);
		if (contest == null) {
			throw new IllegalArgumentException("No contest registered for uuid=" + contestUuid);
		}
		return contest;
	}

	public List<ContestMetadata> searchContests(ContestSearchParameters search) {
		Stream<Contest> contestStream;

		if (search.getContestUuid().isPresent()) {
			UUID uuid = search.getContestUuid().get();
			contestStream = Optional.ofNullable(uuidToContests.get(uuid)).stream();
		} else {
			contestStream = uuidToContests.values().stream();
		}

		Stream<ContestMetadata> metaStream = contestStream.map(c -> c.getContestMetadata());

		if (search.getGameUuid().isPresent()) {
			metaStream = metaStream.filter(c -> c.getGameMetadata().getGameId().equals(search.getGameUuid().get()));
		}

		if (search.isBeingPlayed()) {
			metaStream = metaStream.filter(c -> c.isGameOver());
		}

		if (search.isAcceptPlayers()) {
			metaStream = metaStream.filter(c -> c.isAcceptPlayers());
		}

		return metaStream.collect(Collectors.toList());
	}
}
