package com.eventualconsistency.demo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component

public class KafkaSender {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void send(String topic, String payload) {
        try {
            ListenableFuture send = this.kafkaTemplate.send(topic, payload);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("send kafka error='{}'", e);
        }
    }

}
