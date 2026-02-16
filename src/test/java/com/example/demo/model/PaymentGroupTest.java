package com.example.demo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentGroupTest {

    @Test
    void builder_shouldCreatePaymentGroup() {
        // When
        PaymentGroup group = PaymentGroup.builder()
                .id(1L)
                .debtorIban("EE123456789")
                .debtorName("Test Debtor")
                .requestedExecutionDate(LocalDate.of(2024, 1, 1))
                .build();

        // Then
        assertThat(group.getId()).isEqualTo(1L);
        assertThat(group.getDebtorIban()).isEqualTo("EE123456789");
        assertThat(group.getDebtorName()).isEqualTo("Test Debtor");
        assertThat(group.getRequestedExecutionDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void setPayments_shouldAssignPayments() {
        // Given
        PaymentGroup group = PaymentGroup.builder()
                .debtorIban("EE123456789")
                .debtorName("Test Debtor")
                .requestedExecutionDate(LocalDate.now())
                .build();

        Payment payment = Payment.builder()
                .endToEndIdentification("test-123")
                .currency("EUR")
                .amount(new BigDecimal("100.00"))
                .creditorName("Creditor")
                .creditorIban("EE987654321")
                .remittanceInformation("Test")
                .paymentGroup(group)
                .build();

        List<Payment> payments = new ArrayList<>();
        payments.add(payment);

        // When
        group.setPayments(payments);

        // Then
        assertThat(group.getPayments()).hasSize(1);
        assertThat(group.getPayments().get(0).getEndToEndIdentification()).isEqualTo("test-123");
    }
}
