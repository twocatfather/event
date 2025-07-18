package com.example.finance.infrastructure.outbox;

import com.example.finance.domain.event.DomainEvent;
import com.example.finance.domain.service.DomainEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 10;

    @Transactional
    public void storedEvent(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getClass().getName())
                    .payload(payload)
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store event in outbox", e);
        }
    }

    @Async
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findUnprocessedEventsLimited(BATCH_SIZE);

        for (OutboxEvent event : events) {
            try {
                Class<?> eventClass = Class.forName(event.getEventType());

                DomainEvent domainEvent = (DomainEvent) objectMapper.readValue(event.getPayload(), eventClass);
                eventPublisher.publish(domainEvent);

                event.setProcessed(true);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getEventId(), e);
            }
        }
    }
}
