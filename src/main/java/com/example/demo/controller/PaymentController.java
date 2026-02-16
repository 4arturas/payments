package com.example.demo.controller;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.model.PaymentGroup;
import com.example.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "Endpoints for managing future payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Import payment groups from JSON")
    public ResponseEntity<List<PaymentGroup>> importPayments(@RequestBody List<PaymentGroupRecord> records) {
        log.info("Importing {} payment groups", records.size());
        List<PaymentGroup> result = paymentService.importPayments(records);
        log.info("Successfully imported {} payment groups", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get all payment groups")
    public ResponseEntity<List<PaymentGroup>> getAllPaymentGroups() {
        log.info("Fetching all payment groups");
        List<PaymentGroup> groups = paymentService.getAllGroups();
        log.info("Retrieved {} payment groups", groups.size());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment group by ID")
    public ResponseEntity<PaymentGroup> getPaymentGroup(@PathVariable Long id) {
        log.info("Fetching payment group with id: {}", id);
        PaymentGroup group = paymentService.getGroupById(id);
        log.info("Retrieved payment group: {} with {} payments", id, group.getPayments().size());
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment group by ID")
    public ResponseEntity<Void> deletePaymentGroup(@PathVariable Long id) {
        log.info("Deleting payment group with id: {}", id);
        paymentService.deleteGroup(id);
        log.info("Successfully deleted payment group: {}", id);
        return ResponseEntity.noContent().build();
    }
}
