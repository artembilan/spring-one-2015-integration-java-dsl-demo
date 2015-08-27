package org.springone2015.integration.dsl.demo.xml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("demo-config.xml")
public class SpringOne2015IntegrationXmlDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOne2015IntegrationXmlDemoApplication.class, args);
	}

}
