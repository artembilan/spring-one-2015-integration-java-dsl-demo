package org.springone2015.integration.dsl.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springone2015.integration.dsl.demo.java.SpringOne2015IntegrationJavaDslDemoApplication;

import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringOne2015IntegrationJavaDslDemoApplication.class)
@DirtiesContext
@IntegrationTest("spring.data.mongodb.port=0")
public class SpringOne2015IntegrationJavaDslDemoApplicationTests {

	@Test
	public void testIt() throws Exception {
		Thread.sleep(60_000);
	}

}
