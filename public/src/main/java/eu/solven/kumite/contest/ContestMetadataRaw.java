package eu.solven.kumite.contest;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A serializable view of {@link ContestMetadata}.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class ContestMetadataRaw {
	@NonNull
	UUID contestId;

	@NonNull
	ContestCreationMetadata constantMetadata;

	@NonNull
	ContestDynamicMetadata dynamicMetadata;

}
