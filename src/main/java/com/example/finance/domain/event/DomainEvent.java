package com.example.finance.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *  도메인 이벤트의 기본 클래스
 *  모든 도메인 이벤트가 해당 이벤트를 상속 받아야한다.
 *  각 이벤트들은 고유 ID와 발생 시간을 가진다.
 */
@Getter
@NoArgsConstructor
public abstract class DomainEvent {
     private UUID eventId;
     private LocalDateTime occurredOn;

     protected void initialize() {
         if (this.eventId == null) {
             this.eventId = UUID.randomUUID();
         }

         if (this.occurredOn == null) {
             this.occurredOn = LocalDateTime.now();
         }
     }
}
