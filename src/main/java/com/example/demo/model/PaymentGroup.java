package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "debtor_iban", nullable = false)
    private String debtorIban;

    @Column(name = "debtor_name", nullable = false)
    private String debtorName;

    @Column(name = "requested_execution_date", nullable = false)
    private LocalDate requestedExecutionDate;

    // Audit field
    @Column(name = "created_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private java.time.Instant createdAt;

    // Identification fields
    @Column(name = "external_id", nullable = false, unique = true, length = 255)
    private String externalId;

    @Column(name = "uname", nullable = false, length = 64)
    private String uname;

    @Column(name = "tcif", nullable = false, length = 64)
    private String tcif;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "source_system", nullable = false, length = 64)
    private String sourceSystem;

    @OneToMany(mappedBy = "paymentGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<Payment> payments = new ArrayList<>();

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setPaymentGroup(this);
    }
}
