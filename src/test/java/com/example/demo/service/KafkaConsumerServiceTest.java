package com.example.demo.service;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.dto.PaymentRecord;
import com.example.demo.model.PaymentGroup;
import com.example.demo.repository.PaymentGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentGroupRepository paymentGroupRepository;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private PaymentGroupRecord testRecord;

    @BeforeEach
    void setUp() {
        PaymentRecord.InstructedAmount amount = new PaymentRecord.InstructedAmount("EUR", new BigDecimal("50.00"));
        PaymentRecord.CreditorAccount creditorAccount = new PaymentRecord.CreditorAccount("EE111222333");
        PaymentRecord paymentRecord = new PaymentRecord(
                "kafka-end-to-end",
                amount,
                "Kafka Creditor",
                creditorAccount,
                "Kafka test"
        );

        PaymentGroupRecord.DebtorAccount debtorAccount = new PaymentGroupRecord.DebtorAccount("EE444555666");
        testRecord = new PaymentGroupRecord(
                debtorAccount,
                "Kafka Debtor",
                LocalDate.now(),
                List.of(paymentRecord),
                "kafka-ext-id",
                "kafka-user",
                "kafka-tcif",
                "LV",
                "kafka-system"
        );
    }

    @Test
    void consumeCreated_shouldImportPayment() {
        // Given
        when(paymentService.importPayments(any())).thenReturn(List.of());

        // When
        kafkaConsumerService.consumeCreated(testRecord);

        // Then
        verify(paymentService, times(1)).importPayments(List.of(testRecord));
    }

    @Test
    void consumeDeleted_shouldDeletePayment() {
        // Given
        Long id = 1L;
        doNothing().when(paymentService).deleteGroup(id);

        // When
        kafkaConsumerService.consumeDeleted(id);

        // Then
        verify(paymentService, times(1)).deleteGroup(id);
    }

    @Test
    void consumeDeleted_shouldHandleException_whenGroupNotFound() {
        // Given
        Long id = 999L;
        doThrow(new RuntimeException("Group not found")).when(paymentService).deleteGroup(id);

        // When
        kafkaConsumerService.consumeDeleted(id);

        // Then
        verify(paymentService, times(1)).deleteGroup(id);
        // No exception should be thrown - it's caught and logged
    }
}
