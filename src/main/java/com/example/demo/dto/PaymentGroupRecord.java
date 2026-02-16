package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

public record PaymentGroupRecord(
    DebtorAccount debtorAccount,
    String debtorName,
    LocalDate requestedExecutionDate,
    List<PaymentRecord> payments,
    // Identification fields
    String externalId,
    String uname,
    String tcif,
    String country,
    String sourceSystem
) {
    public record DebtorAccount(String iban) {}
}
