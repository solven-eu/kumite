package eu.solven.kumite.app.webflux.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.solven.kumite.game.IGameMetadataConstants;

/**
 * Holds various details/metadata/enrichment about the application.
 */
@RestController
public class MetadataController {

	@GetMapping("/api/v1/public/metadata")
	@Bean
	public Map<String, ?> getMetadata() {
		Map<String, Object> metadata = new LinkedHashMap<>();

		{
			Map<String, Object> tagsMetadata = new LinkedHashMap<>();
			tagsMetadata.put(IGameMetadataConstants.TAG_OPTIMIZATION,
					"The goal is to find the best solution for given problem. Any player can join.");
			tagsMetadata.put(IGameMetadataConstants.TAG_1V1,
					"We need exactly 2 players, to play one against the other");
			tagsMetadata.put(IGameMetadataConstants.TAG_PERFECT_INFORMATION, "All players can see the whole board");

			metadata.put("tags", tagsMetadata);
		}

		return metadata;
	}
}
