package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_group_id", nullable = false)
    @JsonBackReference
    private PaymentGroup paymentGroup;

    @Column(name = "end_to_end_identification", nullable = false, unique = true)
    private String endToEndIdentification;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "creditor_name", nullable = false)
    private String creditorName;

    @Column(name = "creditor_iban", nullable = false)
    private String creditorIban;

    @Column(name = "remittance_information")
    private String remittanceInformation;
}
