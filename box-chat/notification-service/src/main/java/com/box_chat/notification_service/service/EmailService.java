package com.box_chat.notification_service.service;

import com.box_chat.notification_service.dto.request.EmailRequest;
import com.box_chat.notification_service.dto.request.SendEmailRequest;
import com.box_chat.notification_service.dto.request.UserRequest;
import com.box_chat.notification_service.dto.response.EmailResponse;
import com.box_chat.notification_service.mapper.EmailMapper;
import com.box_chat.notification_service.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    EmailClient emailClient;
    EmailMapper emailMapper;
    @NonFinal
    @Value("${notification.email.brevo.api-key}")
    String apiKey;
    @NonFinal
    @Value("${notification.email.brevo.sender-email}")
    String senderEmail;
    @NonFinal
    @Value("${notification.email.brevo.sender-name}")
    String senderName;

    public EmailResponse sendEmail(EmailRequest emailRequest) {
        SendEmailRequest sendEmailRequest = emailMapper.toSendEmailRequest(emailRequest);
        sendEmailRequest.setSender(createSender());

        log.error("{}", sendEmailRequest);
        try {
            return emailClient.sendEmail(apiKey, sendEmailRequest);
        } catch (FeignException e) {e.printStackTrace();}
        return null;
    }

    private UserRequest createSender() {
        return UserRequest
            .builder()
            .email(senderEmail)
            .name(senderName)
            .build();
    }
}
