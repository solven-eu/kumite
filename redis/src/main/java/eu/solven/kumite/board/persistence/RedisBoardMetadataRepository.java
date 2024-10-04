package eu.solven.kumite.board.persistence;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import eu.solven.kumite.board.BoardDynamicMetadata;
import eu.solven.kumite.redis.RepositoryKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class RedisBoardMetadataRepository implements IBoardMetadataRepository {

	final RedisTemplate<Object, Object> redisTemplate;

	final Environment env;

	private RepositoryKey<UUID> key(UUID contestId) {
		return RepositoryKey.<UUID>builder().storeName(this.getClass().getSimpleName()).actualKey(contestId).build();
	}

	private BoundValueOperations<Object, Object> valueOp(UUID contestId) {
		return redisTemplate.boundValueOps(key(contestId));
	}

	// Contests lives for at most 1 day. This helps preventing Redis OOM.
	Duration getTtl() {
		return Duration.parse(env.getProperty("kumite.redis.ttl", "P1D"));
	}

	@Override
	public Optional<BoardDynamicMetadata> putIfAbsent(UUID contestId, BoardDynamicMetadata board) {
		Boolean result = valueOp(contestId).setIfAbsent(board, getTtl());
		log.info("contestId={} putIfAbsent={}", contestId, result);
		if (Boolean.TRUE.equals(result)) {
			return Optional.empty();
		} else {
			log.warn("Trying to initialize multiple times board for contestId={}", contestId);
			return getBoard(contestId);
		}
	}

	@Override
	public void putIfPresent(UUID contestId, BoardDynamicMetadata board) {
		Boolean result = valueOp(contestId).setIfPresent(board, getTtl());
		log.info("contestId={} putIfAbsent={}", contestId, result);
		if (Boolean.TRUE.equals(result)) {
			log.debug("BoardMetadata updated for contestId={}", contestId);
		} else {
			log.warn("No boardMetadata contestId={}", contestId);
		}
	}

	@Override
	public boolean hasContest(UUID contestId) {
		return redisTemplate.hasKey(key(contestId));
	}

	@Override
	public Optional<BoardDynamicMetadata> getBoard(UUID contestId) {
		BoardDynamicMetadata board = (BoardDynamicMetadata) valueOp(contestId).get();
		return Optional.ofNullable(board);
	}

	@Override
	public void updateBoard(UUID contestId, BoardDynamicMetadata currentBoard) {
		valueOp(contestId).setIfPresent(currentBoard, getTtl());
	}

}
