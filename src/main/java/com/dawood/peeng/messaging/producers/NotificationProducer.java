package com.dawood.peeng.messaging.producers;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.IncidentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendIncidentMail(IncidentEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.INCIDENT_OPENED_ROUTING_KEY,
                event);

    }

    public void sendIncidentResolvedMail(IncidentEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.INCIDENT_RESOLVED_ROUTING_KEY,
                event);

    }

}
