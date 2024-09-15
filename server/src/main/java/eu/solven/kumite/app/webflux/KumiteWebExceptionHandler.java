package eu.solven.kumite.app.webflux;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
// '-2' to have higher priority than the default WebExceptionHandler
@Order(-2)
@Slf4j
public class KumiteWebExceptionHandler implements WebExceptionHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable e) {
		if (e instanceof NoResourceFoundException) {
			// Let the default WebExceptionHandler manage 404
			return Mono.error(e);
		}

		HttpStatus httpStatus;
		if (e instanceof IllegalArgumentException) {
			httpStatus = HttpStatus.BAD_REQUEST;
		} else if (e instanceof LoginRouteButNotAuthenticatedException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		} else {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		exchange.getResponse().setStatusCode(httpStatus);
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
			respondyBodyAsString = new ObjectMapper().writeValueAsString(responseBody);
		} catch (JsonProcessingException ee) {
			log.error("Issue producing responseBody given {}", responseBody, ee);
			respondyBodyAsString = "{\"error_message\":\"something_went_very_wrong\"}";
		}

		byte[] bytes = respondyBodyAsString.getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
		return exchange.getResponse().writeWith(Flux.just(buffer));
	}

}