package eu.solven.kumite.app.properties;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Give access to Git properties through {@link Environment}.
 */
// https://stackoverflow.com/questions/76679347/spring-boot-service-is-not-picking-up-the-git-properties-file-generated-by-the-g
@Component
@Slf4j
@PropertySource(value = { "classpath:git.properties" }, ignoreResourceNotFound = true)
public class GitPropertySourceConfig {

}