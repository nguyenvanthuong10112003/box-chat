package com.box_chat.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailRequest {
    @NotNull
    UserRequest receiver;
    String subject;
    String htmlContent;
}