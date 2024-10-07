package eu.solven.kumite.board.persistence;

import java.util.Optional;
import java.util.UUID;

import eu.solven.kumite.board.BoardDynamicMetadata;

public interface IBoardMetadataRepository {

	/**
	 * 
	 * @param contestId
	 * @param board
	 * @return if alreadyIn, the already present value
	 */
	Optional<BoardDynamicMetadata> putIfAbsent(UUID contestId, BoardDynamicMetadata board);

	boolean hasContest(UUID contestId);

	Optional<BoardDynamicMetadata> getBoard(UUID contestId);

	void updateBoard(UUID contestId, BoardDynamicMetadata board);

	void putIfPresent(UUID contestId, BoardDynamicMetadata board);


}
