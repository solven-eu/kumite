package eu.solven.kumite.app.webflux;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.contest.AccountForbiddenOperation;
import eu.solven.kumite.security.LoginRouteButNotAuthenticatedException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Convert an applicative {@link Throwable} into a relevant {@link HttpStatus}
 * 
 * @author Benoit Lacelle
 *
 */
@Component
// '-2' to have higher priority than the default WebExceptionHandler
@Order(-2)
@Slf4j
// https://stackoverflow.com/questions/51931178/error-handling-in-webflux-with-routerfunction
public class KumiteWebExceptionHandler implements WebExceptionHandler {

	final ObjectMapper objectMapper = KumiteJackson.objectMapper();

	@Override
	public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable e) {
		if (e instanceof NoResourceFoundException) {
			// Let the default WebExceptionHandler manage 404
			return Mono.error(e);
		}

		HttpStatus httpStatus;
		if (e instanceof IllegalArgumentException) {
			httpStatus = HttpStatus.BAD_REQUEST;
		} else if (e instanceof LoginRouteButNotAuthenticatedException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else if (e instanceof AccountForbiddenOperation) {
			httpStatus = HttpStatus.FORBIDDEN;
		} else {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		if (log.isDebugEnabled()) {
			log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage(), e);
		} else {
			log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage());
		}

		Map<String, Object> responseBody = new LinkedHashMap<>();

		if (e.getMessage() == null) {
			responseBody.put("error_message", "");
		} else {
			responseBody.put("error_message", e.getMessage());
		}

		String respondyBodyAsString;
		try {
			respondyBodyAsString = objectMapper.writeValueAsString(responseBody);
		} catch (JsonProcessingException ee) {
			log.error("Issue producing responseBody given {}", responseBody, ee);
			respondyBodyAsString = "{\"error_message\":\"something_went_very_wrong\"}";
		}

		byte[] bytes = respondyBodyAsString.getBytes(StandardCharsets.UTF_8);

		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		DataBuffer buffer = response.bufferFactory().wrap(bytes);
		return response.writeWith(Flux.just(buffer));
	}

}