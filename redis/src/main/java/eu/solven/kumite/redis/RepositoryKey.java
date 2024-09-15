package eu.solven.kumite.redis;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class RepositoryKey<T> {
	String storeName;
	T actualKey;
}
