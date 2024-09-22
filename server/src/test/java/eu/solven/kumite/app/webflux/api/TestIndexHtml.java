package eu.solven.kumite.app.webflux.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.webflux.api.KumiteSpaRouter;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@Import(KumiteSpaRouter.class)
@Slf4j
public class TestIndexHtml {

	@Autowired
	KumiteSpaRouter spaRouter;

	@Test
	public void testIndexHtml() throws IOException {
		String html = spaRouter.indexHtml.getContentAsString(StandardCharsets.UTF_8);
		checkHtmlForUrls(html);
	}

	@Test
	public void testMinifiedIndexHtml() throws IOException {
		String html = spaRouter.minifyHtml(spaRouter.indexHtml.getContentAsString(StandardCharsets.UTF_8));
		checkHtmlForUrls(html);
	}

	private void checkHtmlForUrls(String html) throws IOException {
		Document jsoup = Jsoup.parse(html);

		AtomicInteger nbChecked = new AtomicInteger();

		jsoup.getElementsByAttribute("href").forEach(linkElement -> {
			String href = linkElement.attr("href");

			checkUrl(nbChecked, href);
		});

		jsoup.getElementsByAttributeValue("type", "importmap").forEach(importMap -> {
			String script = importMap.data();

			Map<String, Map<String, String>> asMap;
			try {
				asMap = new ObjectMapper().readValue(script, Map.class);
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException("Invalid json: " + script);
			}

			asMap.get("imports").values().forEach(href -> {
				checkUrl(nbChecked, href);
			});
		});

		Assertions.assertThat(nbChecked.get()).isGreaterThan(5);
	}

	private void checkUrl(AtomicInteger nbChecked, String href) {
		if (href.startsWith("http")) {
			try {
				HttpURLConnection urlConnection = (HttpURLConnection) URI.create(href).toURL().openConnection();

				urlConnection.connect();

				int code = urlConnection.getResponseCode();
				// Check it is a 2XX
				Assertions.assertThat(code / 100).as(href).isEqualTo(2);
			} catch (IOException e) {
				throw new IllegalArgumentException("Invalid href: " + href, e);
			}

			log.info("href={} is valid", href);
			nbChecked.incrementAndGet();
		}
	}
}
