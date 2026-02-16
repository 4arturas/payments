package com.example.demo.service;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.dto.PaymentRecord;
import com.example.demo.model.Payment;
import com.example.demo.model.PaymentGroup;
import com.example.demo.repository.PaymentGroupRepository;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGroupRepository paymentGroupRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentGroupRecord testGroupRecord;
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
        testGroupRecord = new PaymentGroupRecord(
                debtorAccount,
                "Test Debtor",
                LocalDate.now(),
                List.of(paymentRecord),
                "ext-id-123",
                "user123",
                "tcif123",
                "EE",
                "test-system"
        );

        testGroup = PaymentGroup.builder()
                .id(1L)
                .debtorIban("EE987654321")
                .debtorName("Test Debtor")
                .requestedExecutionDate(LocalDate.now())
                .externalId("ext-id-123")
                .uname("user123")
                .tcif("tcif123")
                .country("EE")
                .sourceSystem("test-system")
                .build();
    }

    @Test
    void importPayments_shouldSaveAndEmitEvents() {
        // Given
        when(paymentGroupRepository.save(any(PaymentGroup.class))).thenReturn(testGroup);

        // When
        List<PaymentGroup> result = paymentService.importPayments(List.of(testGroupRecord));

        // Then
        assertThat(result).hasSize(1);
        verify(paymentGroupRepository, times(1)).save(any(PaymentGroup.class));
        verify(kafkaProducerService, times(1)).emitCreated(any(PaymentGroup.class));
    }

    @Test
    void getAllGroups_shouldReturnAllGroups() {
        // Given
        when(paymentGroupRepository.findAll()).thenReturn(List.of(testGroup));

        // When
        List<PaymentGroup> result = paymentService.getAllGroups();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDebtorName()).isEqualTo("Test Debtor");
        verify(paymentGroupRepository, times(1)).findAll();
    }

    @Test
    void getGroupById_shouldReturnGroup_whenExists() {
        // Given
        when(paymentGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // When
        PaymentGroup result = paymentService.getGroupById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDebtorName()).isEqualTo("Test Debtor");
        verify(paymentGroupRepository, times(1)).findById(1L);
    }

    @Test
    void getGroupById_shouldThrowException_whenNotFound() {
        // Given
        when(paymentGroupRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.getGroupById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment group not found");
        verify(paymentGroupRepository, times(1)).findById(999L);
    }

    @Test
    void deleteGroup_shouldDeleteAndEmitEvent() {
        // Given
        when(paymentGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        doNothing().when(paymentGroupRepository).delete(any(PaymentGroup.class));
        doNothing().when(kafkaProducerService).emitDeleted(any());

        // When
        paymentService.deleteGroup(1L);

        // Then
        verify(paymentGroupRepository, times(1)).findById(1L);
        verify(paymentGroupRepository, times(1)).delete(testGroup);
        verify(kafkaProducerService, times(1)).emitDeleted(1L);
    }
}
