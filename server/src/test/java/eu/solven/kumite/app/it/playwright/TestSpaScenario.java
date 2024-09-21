package eu.solven.kumite.app.it.playwright;

import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteContestServerApplication;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { KumiteContestServerApplication.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TestSpaScenario implements IKumiteSpringProfiles {

	@Autowired
	Environment env;

	// https://stackoverflow.com/questions/30312058/spring-boot-how-to-get-the-running-port
	@LocalServerPort
	int randomServerPort;

	@Test
	public void checkSpringProfiles() {
		// Ensure we have loaded games
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_INJECT_DEFAULT_GAMES))).isTrue();

		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_UNSAFE_SERVER))).isTrue();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_FAKEUSER))).isTrue();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_UNSAFE_EXTERNAL_OAUTH2))).isTrue();
	}

	@Disabled("TODO")
	@Test
	public void testScenario() {
		try (Playwright playwright = Playwright.create()) {
			Browser browser = playwright.chromium().launch();
			Page page = browser.newPage();
			page.navigate("http://localhost:" + randomServerPort);

			// Expect a title "to contain" a substring.
			PlaywrightAssertions.assertThat(page).hasTitle(Pattern.compile(".*Kumite.*"));

			// create a locator
			Locator getStarted = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Games"));

			// Expect an attribute "to be strictly equal" to the value.
			PlaywrightAssertions.assertThat(getStarted).hasAttribute("href", "/html/games");

			// Click the get started link.
			getStarted.click();

			// Expects page to have a heading with the name of Installation.
			PlaywrightAssertions
					.assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Installation")))
					.isVisible();
		}
	}
}
