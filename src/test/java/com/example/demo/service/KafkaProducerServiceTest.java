package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually
        ReflectionTestUtils.setField(kafkaProducerService, "createdTopic", "payment.created");
        ReflectionTestUtils.setField(kafkaProducerService, "modifiedTopic", "payment.modified");
        ReflectionTestUtils.setField(kafkaProducerService, "deletedTopic", "payment.deleted");
    }

    @Test
    void emitCreated_shouldSendMessageToCreatedTopic() {
        // Given
        Object payload = "test-payload";

        // When
        kafkaProducerService.emitCreated(payload);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("payment.created"), eq(payload));
    }

    @Test
    void emitModified_shouldSendMessageToModifiedTopic() {
        // Given
        Object payload = "test-payload";

        // When
        kafkaProducerService.emitModified(payload);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("payment.modified"), eq(payload));
    }

    @Test
    void emitDeleted_shouldSendMessageToDeletedTopic() {
        // Given
        Object payload = 123L;

        // When
        kafkaProducerService.emitDeleted(payload);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("payment.deleted"), eq(payload));
    }
}
