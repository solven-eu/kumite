package eu.solven.kumite.tools;

import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;


public class JUGUuidGenerator implements IUuidGenerator {
	
	private RandomBasedGenerator generator;

	public JUGUuidGenerator(RandomGenerator randomGenerator) {
		Random random = Random.from(randomGenerator);
		this.generator = Generators.randomBasedGenerator(random);
	}

	@Override
	public UUID randomUUID() {
		return generator.generate();
	}

}
