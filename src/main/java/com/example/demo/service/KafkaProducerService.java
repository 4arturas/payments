package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // TODO: remove createdTopic from producer
    @Value("${app.kafka.topics.future-payment.created}")
    private String createdTopic;

    @Value("${app.kafka.topics.future-payment.modified}")
    private String modifiedTopic;

    @Value("${app.kafka.topics.future-payment.deleted}")
    private String deletedTopic;

    public void emitCreated(Object payload) {
        log.info("Emitting payment created event to topic: {}", createdTopic);
        kafkaTemplate.send(createdTopic, payload);
    }

    public void emitModified(Object payload) {
        log.info("Emitting payment modified event to topic: {}", modifiedTopic);
        kafkaTemplate.send(modifiedTopic, payload);
    }

    public void emitDeleted(Object payload) {
        log.info("Emitting payment deleted event to topic: {}", deletedTopic);
        kafkaTemplate.send(deletedTopic, payload);
    }
}
