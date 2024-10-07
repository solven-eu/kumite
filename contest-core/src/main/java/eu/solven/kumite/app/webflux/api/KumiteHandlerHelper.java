package eu.solven.kumite.app.webflux.api;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * Helpers around {@link KumiteApiRouter} handlers.
 * 
 * @author Benoit Lacelle
 *
 */
public class KumiteHandlerHelper {

	public static UUID uuid(String rawUuid, String idKey) {
		if ("undefined".equals(rawUuid)) {
			throw new IllegalArgumentException("`undefined` is an invalid `%s`".formatted(idKey));
		} else if (rawUuid == null) {
			throw new IllegalArgumentException("`null` is an invalid `%s`".formatted(idKey));
		}
		return UUID.fromString(rawUuid);
	}

	public static UUID uuid(ServerRequest request, String idKey) {
		Optional<String> optPlayerId = request.queryParam(idKey);
		return uuid(optPlayerId.orElseThrow(() -> new IllegalArgumentException("Lack `%s`".formatted(idKey))), idKey);
	}

	public static Optional<UUID> optUuid(ServerRequest request, String idKey) {
		Optional<String> optUuid = request.queryParam(idKey);

		return optUuid.map(rawUuid -> uuid(rawUuid, idKey));
	}

	public static Optional<UUID> optUuid(Optional<String> optRaw, String idKey) {
		return optRaw.map(raw -> uuid(raw, idKey));
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

	public static Mono<ServerResponse> okAsJson(Object body) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body));
	}

	// public static Mono<ServerResponse> resourceGone(Map<String, ?> body) {
	// return ServerResponse.status(HttpStatus.NOT_FOUND)
	// .contentType(MediaType.APPLICATION_JSON)
	// .body(BodyInserters.fromValue(body));
	// }

}
