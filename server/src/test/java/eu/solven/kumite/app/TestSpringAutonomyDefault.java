package eu.solven.kumite.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { KumiteContestServerApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
public class TestSpringAutonomyDefault implements IKumiteSpringProfiles {

	@Autowired
	ApplicationContext appContest;

	@Test
	public void testSpringProfiles() {
		log.info("startupDate: {}", appContest.getStartupDate());
	}
}
