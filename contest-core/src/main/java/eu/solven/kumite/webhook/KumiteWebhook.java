package eu.solven.kumite.webhook;

import java.net.URI;
import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class KumiteWebhook {
	@NonNull
	UUID webhookId;

	@NonNull
	UUID accountId;

	@NonNull
	UUID gameId;

	@NonNull
	URI webhookUri;

	// a-la-gitlab. Would be added in some header
	// @NonNull
	// String secretToken;
}
