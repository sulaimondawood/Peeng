package com.dawood.peeng.messaging.mails;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender javaMailSender;

  @Value("${app.email}")
  private String from;

  public void send(String to, String subject, String body) {

    try {

      MimeMessage mimeMessage = javaMailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

      helper.setFrom(from, "Peeng");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true);

      javaMailSender.send(mimeMessage);
      log.info("Email successfully sent to {}", to);

    } catch (UnsupportedEncodingException | MessagingException e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
    }

  }

}
