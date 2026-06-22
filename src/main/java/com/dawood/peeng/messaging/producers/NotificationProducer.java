package com.dawood.peeng.messaging.producers;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.IncidentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendIncidentMail(IncidentEvent event) {

        log.info("Opened incident ID inside Send Incident Mail Notification Producer: {}", event.getIncidentId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.INCIDENT_OPENED_ROUTING_KEY,
                event);

    }

    public void sendIncidentResolvedMail(IncidentEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.INCIDENT_CLOSED_ROUTING_KEY,
                event);

    }

}
