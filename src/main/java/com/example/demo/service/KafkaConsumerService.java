package com.example.demo.service;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.model.PaymentGroup;
import com.example.demo.repository.PaymentGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${app.kafka.topics.future-payment.created}")
    @Transactional
    public void consumeCreated(PaymentGroupRecord record) {
        log.info("[KAFKA CONSUMER] Received payment.created event for debtor: {}, external_id: {}", 
                record.debtorName(), record.externalId());
        // Verify if it already exists (idempotency) - in this simple case we just save
        paymentService.importPayments(java.util.List.of(record));
        log.info("[KAFKA CONSUMER] Successfully processed payment.created event for external_id: {}", 
                record.externalId());
    }

    @KafkaListener(topics = "${app.kafka.topics.future-payment.deleted}")
    @Transactional
    public void consumeDeleted(Long id) {
        log.info("[KAFKA CONSUMER] Received payment.deleted event for group id: {}", id);
        try {
            paymentService.deleteGroup(id);
            log.info("[KAFKA CONSUMER] Successfully processed payment.deleted event for id: {}", id);
        } catch (Exception e) {
            log.warn("[KAFKA CONSUMER] Group already deleted or not found: {}, error: {}", id, e.getMessage());
        }
    }
}
