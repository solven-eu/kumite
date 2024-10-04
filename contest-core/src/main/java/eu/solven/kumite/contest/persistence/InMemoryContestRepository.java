package eu.solven.kumite.contest.persistence;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import eu.solven.kumite.contest.ContestCreationMetadata;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryContestRepository implements IContestsRepository {

	final Map<UUID, ContestCreationMetadata> uuidToContests = new ConcurrentHashMap<>();

	@Override
	public Optional<ContestCreationMetadata> putIfAbsent(UUID contestId, ContestCreationMetadata contest) {
		ContestCreationMetadata alreadyIn = uuidToContests.putIfAbsent(contestId, contest);
		return Optional.ofNullable(alreadyIn);
	}

	@Override
	public Optional<ContestCreationMetadata> getById(UUID contestId) {
		return Optional.ofNullable(uuidToContests.get(contestId));
	}

	@Override
	public Stream<Entry<UUID, ContestCreationMetadata>> stream() {
		return uuidToContests.entrySet().stream();
	}

	public void clear() {
		long size = uuidToContests.size();
		log.info("We reset {} contests", size);
		uuidToContests.clear();
	}

//	@Override
//	public void archive(UUID contestId) {
//		// TODO Should we remove the contest?
//	}
}
