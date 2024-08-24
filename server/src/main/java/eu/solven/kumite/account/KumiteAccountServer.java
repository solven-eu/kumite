package eu.solven.kumite.account;

import java.net.URI;
import java.util.UUID;

import lombok.Value;

@Value
public class KumiteAccountServer {
	UUID serverId;

	UUID accountId;

	URI webhookUri;
}
