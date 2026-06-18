package com.dawood.peeng.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "peeng.exchange";

    public static final String SCHEDULER_ROUTING_KEY = "monitor.execute";

    public static final String SCHEDULER_ROUTING_QUEUE = "monitor.execute.queue";

    public static final String EMAIL_QUEUE = "peeng.email.verification.queue";

    public static final String INCIDENT_OPENED_QUEUE = " incident-opened-queue";

    public static final String INCIDENT_CLOSED_QUEUE = "incident-closed-queue";

    public static final String EMAIL_ROUTING_KEY = "peeng.email.verification";

    public static final String INCIDENT_OPENED_ROUTING_KEY = "incident.opened";

    public static final String INCIDENT_RESOLVED_ROUTING_KEY = "incident.resolved";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue incidentOpenedQueue() {
        return new Queue(INCIDENT_OPENED_QUEUE);
    }

    @Bean
    public Binding incidentOpenedBinding(Queue incidentOpenedQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(incidentOpenedQueue)
                .to(exchange)
                .with(INCIDENT_OPENED_ROUTING_KEY);
    }

    @Bean
    public Queue incidentResolvedQueue() {
        return new Queue(INCIDENT_CLOSED_QUEUE);
    }

    @Bean
    public Binding incidentResolvedBinding(TopicExchange exchange, Queue incidentResolvedQueue){
        return BindingBuilder
                .bind(incidentResolvedQueue)
                .to(exchange)
                .with(INCIDENT_RESOLVED_ROUTING_KEY);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE);
    }

    @Bean
    public Queue schedulerQueue() {
        return new Queue(SCHEDULER_ROUTING_QUEUE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue)
                .to(exchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding schedulerBinding(Queue schedulerQueue, TopicExchange exchange) {

        return BindingBuilder.bind(schedulerQueue)
                .to(exchange)
                .with(SCHEDULER_ROUTING_KEY);

    }

    @Bean
    public MessageConverter SimpleMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}