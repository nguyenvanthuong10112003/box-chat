package com.box_chat.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    String channel;
    String receiverName;
    String receiverEmail;
    String subject;
    String htmlContent;
    Map<String, String> params;
}