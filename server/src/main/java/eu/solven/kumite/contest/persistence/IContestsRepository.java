package eu.solven.kumite.contest.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import eu.solven.kumite.contest.ContestCreationMetadata;

/**
 * Store the static/constant metadata of contests.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IContestsRepository {

	/**
	 * 
	 * @param contestId
	 * @param contest
	 * @return the optionally already existing {@link ContestCreationMetadata}
	 */
	Optional<ContestCreationMetadata> putIfAbsent(UUID contestId, ContestCreationMetadata contest);

	Optional<ContestCreationMetadata> getById(UUID contestId);

	Stream<Map.Entry<UUID, ContestCreationMetadata>> stream();

}
