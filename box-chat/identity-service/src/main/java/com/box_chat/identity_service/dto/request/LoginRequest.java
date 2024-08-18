package com.box_chat.identity_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email is invalid")
    String email;
    @NotBlank(message = "Password must be from 5 character")
    @Min(value = 5, message = "Password must be from 5 character")
    String password;
}
