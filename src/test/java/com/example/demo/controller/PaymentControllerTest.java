package com.example.demo.controller;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.dto.PaymentRecord;
import com.example.demo.model.PaymentGroup;
import com.example.demo.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentGroupRecord testRecord;
    private PaymentGroup testGroup;

    @BeforeEach
    void setUp() {
        PaymentRecord.InstructedAmount amount = new PaymentRecord.InstructedAmount("EUR", new BigDecimal("100.00"));
        PaymentRecord.CreditorAccount creditorAccount = new PaymentRecord.CreditorAccount("EE123456789");
        PaymentRecord paymentRecord = new PaymentRecord(
                "end-to-end-123",
                amount,
                "Test Creditor",
                creditorAccount,
                "Test payment"
        );

        PaymentGroupRecord.DebtorAccount debtorAccount = new PaymentGroupRecord.DebtorAccount("EE987654321");
        testRecord = new PaymentGroupRecord(
                debtorAccount,
                "Test Debtor",
                LocalDate.now(),
                List.of(paymentRecord),
                "ctrl-ext-id",
                "ctrl-user",
                "ctrl-tcif",
                "EE",
                "ctrl-system"
        );

        testGroup = PaymentGroup.builder()
                .id(1L)
                .debtorIban("EE987654321")
                .debtorName("Test Debtor")
                .requestedExecutionDate(LocalDate.now())
                .externalId("ctrl-ext-id")
                .uname("ctrl-user")
                .tcif("ctrl-tcif")
                .country("EE")
                .sourceSystem("ctrl-system")
                .build();
    }

    @Test
    void importPayments_shouldReturnOk() throws Exception {
        // Given
        when(paymentService.importPayments(any())).thenReturn(List.of(testGroup));

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(testRecord))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].debtorName").value("Test Debtor"));

        verify(paymentService, times(1)).importPayments(any());
    }

    @Test
    void getAllPaymentGroups_shouldReturnAllGroups() throws Exception {
        // Given
        when(paymentService.getAllGroups()).thenReturn(List.of(testGroup));

        // When & Then
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].debtorName").value("Test Debtor"));

        verify(paymentService, times(1)).getAllGroups();
    }

    @Test
    void getPaymentGroup_shouldReturnGroup() throws Exception {
        // Given
        when(paymentService.getGroupById(1L)).thenReturn(testGroup);

        // When & Then
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtorName").value("Test Debtor"));

        verify(paymentService, times(1)).getGroupById(1L);
    }

    @Test
    void deletePaymentGroup_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(paymentService).deleteGroup(1L);

        // When & Then
        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1)).deleteGroup(1L);
    }
}
