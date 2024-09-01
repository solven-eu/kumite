package eu.solven.kumite.contest;

import java.util.UUID;

public class KumiteHandlerHelper {

	public static UUID uuid(String rawUuid) {
		if ("undefined".equals(rawUuid)) {
			throw new IllegalArgumentException("`undefined` is an invalid id");
		}
		return UUID.fromString(rawUuid);
	}

}
