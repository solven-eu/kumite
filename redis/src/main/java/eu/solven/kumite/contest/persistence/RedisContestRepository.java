package eu.solven.kumite.contest.persistence;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import eu.solven.kumite.contest.ContestCreationMetadata;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.redis.RepositoryKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class RedisContestRepository implements IContestsRepository {

	final RedisTemplate<Object, Object> redisTemplate;

	final Environment env;

	private RepositoryKey<UUID> key(UUID contestId) {
		return RepositoryKey.<UUID>builder().storeName(getClass().getSimpleName()).actualKey(contestId).build();
	}

	// private RepositoryKey<String> wildcardKey() {
	// return RepositoryKey.<String>builder().storeName(getClass().getSimpleName()).actualKey("*").build();
	// }

	private BoundValueOperations<Object, Object> valueOp(UUID contestId) {
		return redisTemplate.boundValueOps(key(contestId));
	}

	// Contests lives for at most 1 day. This helps preventing Redis OOM.
	Duration getTtl() {
		return Duration.parse(env.getProperty("kumite.redis.ttl", "P1D"));
	}

	@Override
	public Optional<ContestCreationMetadata> putIfAbsent(UUID contestId, ContestCreationMetadata contest) {
		Boolean result = valueOp(contestId).setIfAbsent(contest, getTtl());
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
		// RedisSerializer<Object> keySerializer = (RedisSerializer<Object>) redisTemplate.getKeySerializer();
		// RepositoryKey<String> wildcardKey = wildcardKey();
		// byte[] pattern = keySerializer.serialize(wildcardKey);
		// if (pattern == null) {
		// throw new IllegalStateException("Serialization gave null for: " + wildcardKey);
		// }
		ScanOptions options = ScanOptions.scanOptions().match("*" + getClass().getSimpleName() + "*").build();

		// https://stackoverflow.com/questions/19098079/how-to-get-all-keys-from-redis-using-redis-template
		return redisTemplate.scan(options)
				.stream()
				.map(o -> ((RepositoryKey<UUID>) o).getActualKey())
				.map(contestId -> Map.entry(contestId, getById(contestId).orElse(ContestCreationMetadata.empty())))
				.filter(e -> !e.getValue().getGameId().equals(IGameMetadataConstants.EMPTY));
	}
}
