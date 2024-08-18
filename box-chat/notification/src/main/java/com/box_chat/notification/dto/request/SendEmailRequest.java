package com.box_chat.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SendEmailRequest {
    UserRequest sender;
    List<UserRequest> to;
    String subject;
    String htmlContent;
}
