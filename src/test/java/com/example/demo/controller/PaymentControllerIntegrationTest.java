package com.example.demo.controller;

import com.example.demo.dto.PaymentGroupRecord;
import com.example.demo.dto.PaymentRecord;
import com.example.demo.model.PaymentGroup;
import com.example.demo.repository.PaymentGroupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = PaymentControllerIntegrationTest.Initializer.class)
public class PaymentControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentGroupRepository repository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    public void testImportAndCrud() throws Exception {
        repository.deleteAll();
        
        // Prepare data
        PaymentRecord.InstructedAmount amount = new PaymentRecord.InstructedAmount("EUR", new BigDecimal("15.00"));
        PaymentRecord.CreditorAccount cAcc = new PaymentRecord.CreditorAccount("EE701700017001577198");
        PaymentRecord pRecord = new PaymentRecord("end001", amount, "creditor name", cAcc, "minimal");

        PaymentGroupRecord.DebtorAccount dAcc = new PaymentGroupRecord.DebtorAccount("EE021010220208830224");
        PaymentGroupRecord gRecord = new PaymentGroupRecord(
                dAcc, 
                "debtor name", 
                LocalDate.now(), 
                List.of(pRecord),
                "int-test-ext-id-1",
                "int-user-1",
                "int-tcif-1",
                "EE",
                "integration-test"
        );

        // POST Import
        String json = objectMapper.writeValueAsString(List.of(gRecord));
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // GET All
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].debtorName").value("debtor name"));
    }

    @Test
    public void testKafkaCreatedTriggerPersistence() throws Exception {
        repository.deleteAll();

        // Prepare data
        PaymentRecord.InstructedAmount amount = new PaymentRecord.InstructedAmount("EUR", new BigDecimal("25.00"));
        PaymentRecord.CreditorAccount cAcc = new PaymentRecord.CreditorAccount("EE123456789");
        PaymentRecord pRecord = new PaymentRecord("kafka-001", amount, "kafka creditor", cAcc, "kafka-test");

        PaymentGroupRecord.DebtorAccount dAcc = new PaymentGroupRecord.DebtorAccount("EE987654321");
        PaymentGroupRecord gRecord = new PaymentGroupRecord(
                dAcc, 
                "kafka debtor", 
                LocalDate.now(), 
                List.of(pRecord),
                "kafka-test-ext-id",
                "kafka-user",
                "kafka-tcif",
                "LV",
                "kafka-integration-test"
        );

        // Send message to Kafka
        kafkaTemplate.send("payment.created", gRecord);

        // Wait for async processing and verify DB
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<PaymentGroup> groups = repository.findAll();
            assertThat(groups).hasSize(1);
            assertThat(groups.get(0).getDebtorName()).isEqualTo("kafka debtor");
        });
    }
}
