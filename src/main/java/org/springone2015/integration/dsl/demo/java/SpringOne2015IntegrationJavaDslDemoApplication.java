package org.springone2015.integration.dsl.demo.java;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.support.StringStringMapBuilder;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mongodb.outbound.MongoDbStoringMessageHandler;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.TriggerContext;

@SpringBootApplication
public class SpringOne2015IntegrationJavaDslDemoApplication extends IntegrationFlowAdapter {

	public static void main(String[] args) {
		SpringApplication.run(SpringOne2015IntegrationJavaDslDemoApplication.class, args);
	}

	@Value("classpath:application.properties")
	private File applicationProperties;

	@Autowired
	private MongoDbFactory mongoDbFactory;

	private AtomicBoolean invoked = new AtomicBoolean();


	private Date nextExecutionDate(TriggerContext triggerContext) {
		return !this.invoked.getAndSet(true) ? new Date() : null;
	}

	@Override
	protected IntegrationFlowDefinition<?> buildFlow() {
		return from(() -> new GenericMessage<>(this.applicationProperties),
				e -> e.poller(p -> p.trigger(this::nextExecutionDate)))
				.split(Files.splitter())
				.channel(c -> c.executor(Executors.newCachedThreadPool()))
				.<String, String[]>transform(p -> p.split("="))
				.transform((String[] p) ->
						new StringStringMapBuilder()
								.put("key", p[0])
								.put("value", p[1])
								.get())
				.publishSubscribeChannel(ps -> ps
								.subscribe(f -> f.handle(new LoggingHandler(LoggingHandler.Level.INFO.name())))
								.subscribe(f -> f.handle(mongoDbStoringMessageHandler()))
				);
	}

	private MessageHandler mongoDbStoringMessageHandler() {
		MongoDbStoringMessageHandler handler = new MongoDbStoringMessageHandler(this.mongoDbFactory);
		handler.setCollectionNameExpression(new LiteralExpression("SpringOne2015IntegrationJavaDslDemoApplication"));
		return handler;
	}

}
