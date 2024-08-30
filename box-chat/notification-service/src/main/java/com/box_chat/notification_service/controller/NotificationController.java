package com.box_chat.notification_service.controller;

import com.box_chat.event.dto.NotificationEvent;
import com.box_chat.notification_service.dto.request.EmailRequest;
import com.box_chat.notification_service.mapper.EmailMapper;
import com.box_chat.notification_service.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationController {
    EmailService emailService;
    @KafkaListener(topics = "notification--send-email")
    public void sendEmailNotificationListener(NotificationEvent notificationEvent) {
        log.error("{}", notificationEvent);
        emailService.sendEmail(notificationEvent);
    }
}
