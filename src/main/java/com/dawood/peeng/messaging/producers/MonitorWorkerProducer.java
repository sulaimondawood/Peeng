package com.dawood.peeng.messaging.producers;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.monitor.events.MonitorTaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorWorkerProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendScheduledMonitor(MonitorTaskMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.SCHEDULER_ROUTING_KEY,
                message);

    }

}
