package eu.solven.kumite.app.controllers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.web.reactive.function.server.ServerRequest;

import eu.solven.kumite.app.webflux.KumiteRouter;

/**
 * Helpers around {@link KumiteRouter} handlers.
 * 
 * @author Benoit Lacelle
 *
 */
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

	public static UUID uuid(ServerRequest request, String idKey) {
		Optional<String> optPlayerId = request.queryParam(idKey);
		return uuid(optPlayerId.orElseThrow(() -> new IllegalArgumentException("Lack `%s`".formatted(idKey))));
	}

	public static Optional<UUID> optUuid(ServerRequest request, String idKey) {
		Optional<String> optUuid = request.queryParam(idKey);

		return optUuid.map(rawUuid -> uuid(rawUuid));
	}

	public static Optional<Boolean> optBoolean(ServerRequest request, String idKey) {
		Optional<String> optBoolean = request.queryParam(idKey);

		return optBoolean.map(rawBoolean -> {
			if ("undefined".equals(rawBoolean)) {
				throw new IllegalArgumentException("`undefined` is an invalid rawBoolean");
			}

			return Boolean.parseBoolean(rawBoolean);
		});
	}

}
