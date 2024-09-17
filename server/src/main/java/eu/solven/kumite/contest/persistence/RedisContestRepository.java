package eu.solven.kumite.contest.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import eu.solven.kumite.contest.ContestCreationMetadata;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.redis.RepositoryKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class RedisContestRepository implements IContestsRepository {

	final RedisTemplate<Object, Object> redisTemplate;

	private RepositoryKey<UUID> key(UUID contestId) {
		return RepositoryKey.<UUID>builder().storeName(this.getClass().getSimpleName()).actualKey(contestId).build();
	}

	private BoundValueOperations<Object, Object> valueOp(UUID contestId) {
		return redisTemplate.boundValueOps(key(contestId));
	}

	@Override
	public Optional<ContestCreationMetadata> putIfAbsent(UUID contestId, ContestCreationMetadata contest) {
		Boolean result = valueOp(contestId).setIfAbsent(contest);
		if (Boolean.TRUE.equals(result)) {
			log.info("contestId={} putIfAbsent={}", contestId, result);
			return Optional.empty();
		} else {
			return getById(contestId);
		}
	}

	@Override
	public Optional<ContestCreationMetadata> getById(UUID contestId) {
		Object asMap = valueOp(contestId).get();
		ContestCreationMetadata board = (ContestCreationMetadata) asMap;
		return Optional.ofNullable(board);
	}

	@Override
	public Stream<Map.Entry<UUID, ContestCreationMetadata>> stream() {
		return redisTemplate.keys("")
				.stream()
				.map(o -> ((RepositoryKey<UUID>) o).getActualKey())
				.map(contestId -> Map.entry(contestId, getById(contestId).orElse(ContestCreationMetadata.empty())))
				.filter(e -> !e.getValue().getGameId().equals(IGameMetadataConstants.EMPTY));
	}
}
