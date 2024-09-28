package eu.solven.kumite.webhook;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
public class WebhooksRegistry {

	@Getter(AccessLevel.NONE)
	Map<UUID, List<KumiteWebhook>> accountToWebhooks = new ConcurrentHashMap<>();

	KumiteWebhook registerWebhook(KumiteWebhook kumiteWebhook) {
		List<KumiteWebhook> webhooks =
				accountToWebhooks.computeIfAbsent(kumiteWebhook.getAccountId(), k -> new CopyOnWriteArrayList<>());

		if (webhooks.size() > 128) {
			throw new IllegalStateException("There is already " + webhooks.size() + " webhooks");
		}

		webhooks.add(kumiteWebhook);

		return kumiteWebhook;
	}

	boolean dropWebhook(WebhooksDropParameters drop) {
		List<KumiteWebhook> webhooks =
				accountToWebhooks.computeIfAbsent(drop.getAccountId(), k -> new CopyOnWriteArrayList<>());
		
		boolean dropped = webhooks.removeIf(wh -> wh.getWebhookId().equals(drop.getWebhookId()));

		return dropped;
	}

	public List<KumiteWebhook> listWebhooks(WebhooksSearchParameters search) {
		return accountToWebhooks.getOrDefault(search.getAccountId(), Collections.emptyList());
	}
}
