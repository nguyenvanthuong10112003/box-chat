package com.box_chat.notification.service;

import com.box_chat.notification.dto.request.EmailRequest;
import com.box_chat.notification.dto.request.SendEmailRequest;
import com.box_chat.notification.dto.request.UserRequest;
import com.box_chat.notification.dto.response.EmailResponse;
import com.box_chat.notification.mapper.EmailMapper;
import com.box_chat.notification.repository.httpclient.EmailClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

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
        return emailClient.sendEmail(apiKey, sendEmailRequest);
    }

    private UserRequest createSender() {
        return UserRequest
            .builder()
            .email(senderEmail)
            .name(senderName)
            .build();
    }
}
