package com.box_chat.notification.repository.httpclient;

import com.box_chat.notification.dto.request.SendEmailRequest;
import com.box_chat.notification.dto.response.EmailResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${notification.email.brevo.url}", name = "email-client")
public interface EmailClient {
    @PostMapping(value = "/v3/smtp/email", produces = MediaType.APPLICATION_JSON_VALUE)
    EmailResponse sendEmail(@RequestHeader("api-key") String apiKey, @RequestBody @Valid SendEmailRequest emailRequest);
}
