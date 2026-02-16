package com.example.demo.service;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.dto.PaymentRecord;
import com.example.demo.model.Payment;
import com.example.demo.model.PaymentGroup;
import com.example.demo.repository.PaymentGroupRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentGroupRepository paymentGroupRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public List<PaymentGroup> importPayments(List<PaymentGroupRecord> groupRecords) {
        log.info("Starting import of {} payment group records", groupRecords.size());
        
        List<PaymentGroup> groups = groupRecords.stream()
                .map(this::convertToEntity)
                .map(paymentGroupRepository::save)
                .collect(Collectors.toList());
        
        log.info("Saved {} payment groups to database", groups.size());
        groups.forEach(kafkaProducerService::emitCreated);
        log.info("Emitted Kafka events for {} payment groups", groups.size());
        
        return groups;
    }

    private PaymentGroup convertToEntity(PaymentGroupRecord record) {
        log.debug("Converting payment group record for debtor: {} with external_id: {}", 
                record.debtorName(), record.externalId());
        
        PaymentGroup group = PaymentGroup.builder()
                .debtorIban(record.debtorAccount().iban())
                .debtorName(record.debtorName())
                .requestedExecutionDate(record.requestedExecutionDate())
                .externalId(record.externalId())
                .uname(record.uname())
                .tcif(record.tcif())
                .country(record.country())
                .sourceSystem(record.sourceSystem())
                .build();

        List<Payment> payments = record.payments().stream()
                .map(p -> convertToEntity(p, group))
                .collect(Collectors.toList());

        group.setPayments(payments);
        log.debug("Converted payment group with {} payments", payments.size());
        return group;
    }

    private Payment convertToEntity(PaymentRecord record, PaymentGroup group) {
        return Payment.builder()
                .paymentGroup(group)
                .endToEndIdentification(record.endToEndIdentification())
                .currency(record.instructedAmount().currency())
                .amount(record.instructedAmount().amount())
                .creditorName(record.creditorName())
                .creditorIban(record.creditorAccount().iban())
                .remittanceInformation(record.remittanceInformationUnstructured())
                .build();
    }

    public List<PaymentGroup> getAllGroups() {
        log.info("Fetching all payment groups from database");
        List<PaymentGroup> groups = paymentGroupRepository.findAll();
        log.info("Found {} payment groups", groups.size());
        return groups;
    }

    public PaymentGroup getGroupById(Long id) {
        log.info("Fetching payment group by id: {}", id);
        return paymentGroupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Payment group not found with id: {}", id);
                    return new RuntimeException("Payment group not found: " + id);
                });
    }

    @Transactional
    public void deleteGroup(Long id) {
        log.info("Deleting payment group with id: {}", id);
        PaymentGroup group = getGroupById(id);
        paymentGroupRepository.delete(group);
        log.info("Deleted payment group from database: {}", id);
        kafkaProducerService.emitDeleted(id);
        log.info("Emitted delete event for payment group: {}", id);
    }
}
