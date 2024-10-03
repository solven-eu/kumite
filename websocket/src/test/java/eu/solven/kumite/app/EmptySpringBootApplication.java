package eu.solven.kumite.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;

/**
 * Used to test minimal {@link SpringBootApplication}, still loading `application[-XXX].yml` files
 * 
 * @author Benoit Lacelle
 *
 */
@SpringBootApplication(scanBasePackages = "none", exclude = GraphQlAutoConfiguration.class)
public class EmptySpringBootApplication {

}
