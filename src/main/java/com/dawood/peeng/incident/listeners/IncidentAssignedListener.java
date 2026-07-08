package com.dawood.peeng.incident.listeners;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.incident.events.IncidentAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class IncidentAssignedListener {
    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener
    public void sendAssignmentNotification(IncidentAssignedEvent event){
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.INCIDENT_OPENED_ROUTING_KEY,
                event);

    }
}
