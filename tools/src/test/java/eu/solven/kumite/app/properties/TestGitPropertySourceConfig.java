package eu.solven.kumite.app.properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.tools.GitPropertySourceConfig;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
// @SpringBootTest(classes = { EmptySpringBootApplication.class, GitPropertySourceConfig.class },
// webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(GitPropertySourceConfig.class)
@Slf4j
public class TestGitPropertySourceConfig {

	@Autowired
	Environment env;

	@Test
	public void testSpringProfiles() {
		Assertions.assertThat(env.getRequiredProperty("git.commit.id.abbrev")).isNotNull();
	}
}