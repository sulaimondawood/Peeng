package com.dawood.peeng.messaging.producers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailProducer {

  private final RabbitTemplate rabbitTemplate;

  public void sendVerificationEmail(SendVerificationEmailEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.EXCHANGE,
        RabbitMQConfig.EMAIL_ROUTING_KEY,
        event);
  }

}
