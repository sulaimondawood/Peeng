package com.dawood.peeng.messaging.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;
import com.dawood.peeng.messaging.mails.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

  private final EmailService emailService;
  private final TemplateEngine templateEngine;

  @Value("${app.client-url}")
  private String clientUrl;

  @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
  public void consumeVerificationEmail(SendVerificationEmailEvent event) {

    log.info("Received email verification event for: {}", event.getEmail());
    try {

      String activationLink = String.format("%s/auth/verify?token=%s", clientUrl, event.getToken());

      Context context = new Context();
      context.setVariable("name", event.getName());
      context.setVariable("expiresIn", "15mins");
      context.setVariable("activationLink", activationLink);

      String body = templateEngine.process("email-verification", context);

      emailService.send(event.getEmail(), "Account verification - Peeng", body);

    } catch (Exception e) {
      log.error("Error processing email template or sending message", e);

      throw e;
    }

  }

}
