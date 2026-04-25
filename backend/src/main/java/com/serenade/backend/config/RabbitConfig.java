package com.serenade.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "serenade.tracks";
    public static final String QUEUE_TRANSCODER = "serenade.transcoder";
    public static final String KEY_UPLOADED = "track.uploaded";

    @Bean
    public TopicExchange trackExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue transcoderQueue() {
        return QueueBuilder.durable(QUEUE_TRANSCODER).build();
    }

    @Bean
    public Binding transcoderBinding(Queue transcoderQueue, TopicExchange trackExchange) {
        return BindingBuilder.bind(transcoderQueue).to(trackExchange).with(KEY_UPLOADED);
    }
}
