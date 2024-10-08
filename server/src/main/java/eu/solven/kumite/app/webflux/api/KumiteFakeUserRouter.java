package eu.solven.kumite.app.webflux.api;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import lombok.extern.slf4j.Slf4j;

/**
 * In case of fakeUser, we may want to enable addition routes, like resetServer for integration-tests purposes.
 * 
 * @author Benoit Lacelle
 *
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
@Profile(IKumiteSpringProfiles.P_FAKEUSER + " & " + IKumiteSpringProfiles.P_INMEMORY)
@Import({

		KumiteClearHandler.class

})
public class KumiteFakeUserRouter {

	private RequestPredicate json(String route) {
		return RequestPredicates.path(route).and(RequestPredicates.accept(MediaType.APPLICATION_JSON));
	}

	@Bean
	public RouterFunction<ServerResponse> fakeUserRoutes(KumiteClearHandler kumiteResetHandler) {
		return SpringdocRouteBuilder.route()
				.POST(json("/api/v1/clear"),
						kumiteResetHandler::clear,
						ops -> ops.operationId("clear")
								.response(responseBuilder().responseCode("200").description("Cleared")))
				// One can clear with GET to make it easier for a human
				.GET(json("/api/v1/clear"),
						kumiteResetHandler::clear,
						ops -> ops.operationId("clear")
								.response(responseBuilder().responseCode("200").description("Cleared")))

				.build();

	}
}