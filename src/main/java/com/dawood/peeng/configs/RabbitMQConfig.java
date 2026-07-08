package com.dawood.peeng.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "peeng.exchange";

    // Global DLQ for notification/processing failures
    public static final String DLX_EXCHANGE = "peeng.dlx"; // Dead Letter Exchange
    public static final String DEAD_LETTER_QUEUE = "peeng.dead-letter.queue";
    public static final String DLX_ROUTING_KEY = "peeng.dead-letter.fallback";

    public static final String SCHEDULER_ROUTING_KEY = "monitor.execute";
    public static final String SCHEDULER_ROUTING_QUEUE = "monitor.execute.queue";

    public static final String EMAIL_QUEUE = "peeng.email.verification.queue";
    public static final String EMAIL_ROUTING_KEY = "peeng.email.verification";

    public static final String INCIDENT_OPENED_QUEUE = "incident.opened.queue";
    public static final String INCIDENT_OPENED_ROUTING_KEY = "incident.opened";

    public static final String INCIDENT_CLOSED_QUEUE = "incident.closed.queue";
    public static final String INCIDENT_CLOSED_ROUTING_KEY = "incident.closed";

    public static final String  INCIDENT_ASSIGNED_TO_QUEUE= "incident.assigned.queue";
    public static final String INCIDENT_ASSIGNED_TO_MEMBER_ROUTING_KEY="incident.assigned";


    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue(){
        return  new Queue(DEAD_LETTER_QUEUE);
    }

    @Bean
    public Binding dlxBinding(TopicExchange deadLetterExchange, Queue deadLetterQueue){
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DLX_ROUTING_KEY);
    }

    @Bean
    public Queue incidentOpenedQueue() {

        return QueueBuilder.durable(INCIDENT_OPENED_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING_KEY)
                .build();
    }



    @Bean
    public Queue incidentResolvedQueue() {

        return  QueueBuilder.durable(INCIDENT_CLOSED_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING_KEY)
                .build();

    }

    @Bean
    public Binding incidentResolvedBinding(TopicExchange topicExchange, Queue incidentResolvedQueue){
        return BindingBuilder
                .bind(incidentResolvedQueue)
                .to(topicExchange)
                .with(INCIDENT_CLOSED_ROUTING_KEY);
    }

    @Bean
    public Queue emailQueue() {
        return  QueueBuilder.durable(EMAIL_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue schedulerQueue() {
        return QueueBuilder.durable(SCHEDULER_ROUTING_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING_KEY)
                .build();

    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(topicExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding incidentOpenedBinding(Queue incidentOpenedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(incidentOpenedQueue)
                .to(topicExchange)
                .with(INCIDENT_OPENED_ROUTING_KEY);
    }

    @Bean
    public Binding schedulerBinding(Queue schedulerQueue, TopicExchange topicExchange) {

        return BindingBuilder.bind(schedulerQueue)
                .to(topicExchange)
                .with(SCHEDULER_ROUTING_KEY);

    }

    @Bean
    public Queue incidentAssignedQueue(){
        return QueueBuilder
                .durable(INCIDENT_ASSIGNED_TO_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING_KEY)
                .build();
    }

    public Binding incidentAssignedBinding(TopicExchange topicExchange, Queue incidentAssignedQueue){
        return BindingBuilder.bind(incidentAssignedQueue)
                .to(topicExchange)
                .with(RabbitMQConfig.INCIDENT_ASSIGNED_TO_MEMBER_ROUTING_KEY);
    }


    @Bean
    public MessageConverter simpleMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter converter
    ) {

        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);

        template.setMessageConverter(converter);

        return template;
    }

}