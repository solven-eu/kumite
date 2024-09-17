package eu.solven.kumite.app.webflux;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Redirect the SinglePageApplication routes to index.html content.
 * 
 * @author Benoit Lacelle
 *
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class KumiteSpaRouter {

	@Value("classpath:/static/index.html")
	private Resource indexHtml;

	// https://github.com/springdoc/springdoc-openapi-demos/tree/2.x/springdoc-openapi-spring-boot-2-webflux-functional
	// https://stackoverflow.com/questions/6845772/should-i-use-singular-or-plural-name-convention-for-rest-resources
	@Bean
	public RouterFunction<ServerResponse> spaRoutes(Environment env) {
		if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_HEROKU))) {
			log.info("We should rely on PRD resources in `index.html`");
		}

		Mono<ServerResponse> responseIndexHtml =
				ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml);

		return SpringdocRouteBuilder.route()

				// The following routes are useful for the SinglePageApplication
				.GET(RequestPredicates.GET("/html/**").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
						request -> responseIndexHtml,
						ops -> ops.operationId("spaToRoute"))
				.GET(RequestPredicates.GET("/login").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
						request -> responseIndexHtml,
						ops -> ops.operationId("spaToLogin"))

				.build();
	}
}