package org.springone2015.integration.dsl.demo.reactivesteams;

import org.reactivestreams.Publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.http.Http;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.integration.stream.CharacterStreamWritingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MultiValueMap;

import reactor.rx.Streams;

/**
 * @author Artem Bilan
 */
@SpringBootApplication
public class SpringOne2015IntegrationReactiveStreamsDemoApplication {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext =
				SpringApplication.run(SpringOne2015IntegrationReactiveStreamsDemoApplication.class);

		Publisher<Message<String>> publisher = applicationContext.getBean(Publisher.class);
		Streams
				.wrap(publisher)
				.map(m -> MessageBuilder
						.withPayload(m.getPayload().toUpperCase())
						.copyHeaders(m.getHeaders())
						.build())
				.consume(m -> m
						.getHeaders()
						.get(MessageHeaders.REPLY_CHANNEL, MessageChannel.class)
						.send(m));
	}

	@Bean
	public IntegrationFlow httpFlow() {
		return IntegrationFlows
				.from(Http
						.inboundGateway("/service")
						.<MultiValueMap<String, String>>payloadFunction(e -> e.getBody().getFirst("name"))
						.replyTimeout(-1))
				.channel("process")
				.get();
	}

	@Bean
	public IntegrationFlow commandLineFlow() {
		return IntegrationFlows
				.from(CharacterStreamReadingMessageSource.stdin(), e ->
						e.poller(p -> p.fixedDelay(10)))
				.enrichHeaders(h -> h.header(MessageHeaders.REPLY_CHANNEL, stdoutChannel()))
				.channel("process")
				.get();
	}

	@Bean
	public MessageChannel stdoutChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow stdoutFlow() {
		return IntegrationFlows
				.from(stdoutChannel())
				.handle(CharacterStreamWritingMessageHandler.stdout())
				.get();
	}

	@Bean
	public Publisher<Message<String>> reactiveStreamFlow() {
		return IntegrationFlows
				.from("process")
				.transform("Hello "::concat)
				.toReactivePublisher();
	}

}
