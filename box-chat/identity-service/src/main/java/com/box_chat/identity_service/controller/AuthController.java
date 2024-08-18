package com.box_chat.identity_service.controller;

import com.box_chat.identity_service.dto.request.AccountCreationRequest;
import com.box_chat.identity_service.dto.response.ResponseAPI;
import com.box_chat.identity_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {
    AccountService accountService;

    @PostMapping("/create")
    public ResponseAPI<String> createAccount(@RequestBody @Valid AccountCreationRequest accountCreationRequest) {
        return ResponseAPI.success(accountService.createAccount(accountCreationRequest));
    }
}
