package com.dawood.peeng.messaging.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;

@Component
public class EmailConsumer {

  @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
  public void consumeVerificationEmail(SendVerificationEmailEvent event) {

  }

}
