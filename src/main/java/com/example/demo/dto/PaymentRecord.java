package com.example.demo.dto;

import java.math.BigDecimal;

public record PaymentRecord(
    String endToEndIdentification,
    InstructedAmount instructedAmount,
    String creditorName,
    CreditorAccount creditorAccount,
    String remittanceInformationUnstructured
) {
    public record InstructedAmount(String currency, BigDecimal amount) {}
    public record CreditorAccount(String iban) {}
}
