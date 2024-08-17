package com.box_chat.identity_service.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String alias;
    boolean gender;
    String fullName;
    LocalDate dob;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    Account account;

    public int getAge() {
        return Period.between(dob, LocalDate.now()).getYears();
    }
}
