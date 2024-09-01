package eu.solven.kumite.contest;

import java.util.Optional;
import java.util.UUID;

public class KumiteHandlerHelper {

	public static UUID uuid(String rawUuid) {
		if ("undefined".equals(rawUuid)) {
			throw new IllegalArgumentException("`undefined` is an invalid id");
		}
		return UUID.fromString(rawUuid);
	}

	public static UUID uuid(Optional<String> optRawId) {
		if (optRawId.isEmpty()) {
			throw new IllegalArgumentException("Lack required id");
		}
		return uuid(optRawId.get());
	}

}
