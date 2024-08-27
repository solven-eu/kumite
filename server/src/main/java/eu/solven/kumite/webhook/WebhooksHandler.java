package eu.solven.kumite.webhook;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.webhook.KumiteWebhook.KumiteWebhookBuilder;
import eu.solven.kumite.webhook.WebhooksDropParameters.WebhooksDropParametersBuilder;
import eu.solven.kumite.webhook.WebhooksSearchParameters.WebhooksSearchParametersBuilder;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class WebhooksHandler {
	WebhooksRegistry webhookRegistry;

	public Mono<ServerResponse> listWebhooks(ServerRequest request) {
		WebhooksSearchParametersBuilder parameters = WebhooksSearchParameters.builder();

		Optional<String> optId = request.queryParam("account_id");
		optId.ifPresent(rawId -> parameters.accountId(UUID.fromString(rawId)));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(webhookRegistry.listWebhooks(parameters.build())));
	}

	public Mono<ServerResponse> registerWebhook(ServerRequest request) {
		KumiteWebhookBuilder webhookBuilder = KumiteWebhook.builder();

		webhookBuilder.accountId(UUID.fromString(request.queryParam("account_id").get()));
		webhookBuilder.gameId(UUID.fromString(request.queryParam("game_id").get()));

		webhookBuilder.webhookId(UUID.randomUUID());

		webhookBuilder.webhookUri(URI.create(request.queryParam("webhook_uri").get()));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(webhookRegistry.registerWebhook(webhookBuilder.build())));
	}

	public Mono<ServerResponse> dropWebhooks(ServerRequest request) {
		WebhooksDropParametersBuilder webhookBuilder = WebhooksDropParameters.builder();

		webhookBuilder.accountId(UUID.fromString(request.queryParam("account_id").get()));
		webhookBuilder.webhookId(UUID.fromString(request.queryParam("webhook_id").get()));

		boolean dropped = webhookRegistry.dropWebhook(webhookBuilder.build());
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of("dropped", dropped)));
	}
}