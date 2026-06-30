package com.dawood.peeng.messaging.producers;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.dawood.peeng.configs.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorWorkerProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendScheduledMonitor(UUID monitorId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.SCHEDULER_ROUTING_KEY,
                monitorId);

    }

}
