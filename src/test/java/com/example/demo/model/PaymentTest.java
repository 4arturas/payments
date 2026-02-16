package com.example.demo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    void builder_shouldCreatePayment() {
        // Given
        PaymentGroup group = PaymentGroup.builder()
                .id(1L)
                .debtorIban("EE123456789")
                .debtorName("Test Debtor")
                .build();

        // When
        Payment payment = Payment.builder()
                .id(1L)
                .paymentGroup(group)
                .endToEndIdentification("end-to-end-123")
                .currency("EUR")
                .amount(new BigDecimal("100.00"))
                .creditorName("Test Creditor")
                .creditorIban("EE987654321")
                .remittanceInformation("Test payment")
                .build();

        // Then
        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getEndToEndIdentification()).isEqualTo("end-to-end-123");
        assertThat(payment.getCurrency()).isEqualTo("EUR");
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(payment.getCreditorName()).isEqualTo("Test Creditor");
        assertThat(payment.getCreditorIban()).isEqualTo("EE987654321");
        assertThat(payment.getRemittanceInformation()).isEqualTo("Test payment");
        assertThat(payment.getPaymentGroup()).isEqualTo(group);
    }

    @Test
    void setters_shouldUpdatePaymentFields() {
        // Given
        Payment payment = Payment.builder().build();

        // When
        payment.setEndToEndIdentification("new-id");
        payment.setCurrency("USD");
        payment.setAmount(new BigDecimal("200.00"));

        // Then
        assertThat(payment.getEndToEndIdentification()).isEqualTo("new-id");
        assertThat(payment.getCurrency()).isEqualTo("USD");
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }
}
