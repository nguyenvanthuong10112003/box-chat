package com.box_chat.identity_service.entity;

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
    String authCode;
    LocalDateTime authTimeExpire;
    boolean verified;

    @PrePersist
    void onCreate() {
        this.timeCreate = LocalDateTime.now();
    }
}
