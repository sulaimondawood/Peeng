package com.dawood.peeng.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

  public static final String EMAIL_ROUTING_KEY = "peeng.email.verification";

  @Bean
  public TopicExchange topicExchange() {
    return new TopicExchange(EXCHANGE);
  }

  @Bean
  public Queue emailQueue() {
    return new Queue(EMAIL_QUEUE);
  }

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