package com.serenade.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "serenade.tracks";
    public static final String QUEUE_TRANSCODER = "serenade.transcoder";
    public static final String QUEUE_SUBTITLER = "serenade.subtitler";
    public static final String KEY_UPLOADED = "track.uploaded";
    public static final String DLQ_EXCHANGE = "serenade.dead-letter";
    public static final String QUEUE_TRANSCODER_DLQ = "serenade.transcoder.dlq";
    public static final String QUEUE_SUBTITLER_DLQ = "serenade.subtitler.dlq";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public TopicExchange trackExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLQ_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue transcoderQueue() {
        return QueueBuilder.durable(QUEUE_TRANSCODER)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_TRANSCODER)
                .build();
    }

    @Bean
    public Queue transcoderDlq() {
        return QueueBuilder.durable(QUEUE_TRANSCODER_DLQ).build();
    }

    @Bean
    public Binding transcoderBinding(Queue transcoderQueue, TopicExchange trackExchange) {
        return BindingBuilder.bind(transcoderQueue).to(trackExchange).with(KEY_UPLOADED);
    }

    @Bean
    public Binding transcoderDlqBinding(Queue transcoderDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(transcoderDlq).to(deadLetterExchange).with(QUEUE_TRANSCODER);
    }

    @Bean
    public Queue subtitlerQueue() {
        return QueueBuilder.durable(QUEUE_SUBTITLER)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_SUBTITLER)
                .build();
    }

    @Bean
    public Queue subtitlerDlq() {
        return QueueBuilder.durable(QUEUE_SUBTITLER_DLQ).build();
    }

    @Bean
    public Binding subtitlerBinding(Queue subtitlerQueue, TopicExchange trackExchange) {
        return BindingBuilder.bind(subtitlerQueue).to(trackExchange).with(KEY_UPLOADED);
    }

    @Bean
    public Binding subtitlerDlqBinding(Queue subtitlerDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(subtitlerDlq).to(deadLetterExchange).with(QUEUE_SUBTITLER);
    }
}
