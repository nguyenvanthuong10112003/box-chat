package com.box_chat.notification_service.controller;

import com.box_chat.notification_service.dto.request.EmailRequest;
import com.box_chat.notification_service.dto.response.EmailResponse;
import com.box_chat.notification_service.service.EmailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    EmailService emailService;

    @PostMapping("/send")
    public EmailResponse sendEmail(@RequestBody @Valid EmailRequest emailRequest) {
        return emailService.sendEmail(emailRequest);
    }
}
