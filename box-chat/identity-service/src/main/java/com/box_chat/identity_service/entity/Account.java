package com.box_chat.identity_service.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(nullable = false, unique = true)
    String email;
    @Column(nullable = false)
    String password;
    LocalDateTime timeCreate;
    String authenticationCode;
    LocalDateTime authenticationTimeExpire;

    @PrePersist
    void onCreate() {
        this.timeCreate = LocalDateTime.now();
    }
}
