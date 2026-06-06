package com.fintrack.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fintrack.event.TransactionCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String TOPIC = "transaction-created";
    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Transaction event published for userId: {}, offset: {}",
                        event.getUserId(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish transaction event: {}", ex.getMessage());
                }
            });
    }
}
