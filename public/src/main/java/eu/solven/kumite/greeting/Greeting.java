package eu.solven.kumite.greeting;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Used to check API availability
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class Greeting {
	String message;
}