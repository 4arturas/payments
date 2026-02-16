package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.future-payment.created}")
    private String createdTopic;

    @Value("${app.kafka.topics.future-payment.modified}")
    private String modifiedTopic;

    @Value("${app.kafka.topics.future-payment.deleted}")
    private String deletedTopic;

    @Bean
    public NewTopic createdTopic() {
        return TopicBuilder.name(createdTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic modifiedTopic() {
        return TopicBuilder.name(modifiedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deletedTopic() {
        return TopicBuilder.name(deletedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
