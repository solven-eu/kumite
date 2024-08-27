package eu.solven.kumite.webhook;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class WebhooksDropParameters {
	@NonNull
	UUID accountId;

	@NonNull
	UUID webhookId;
}
