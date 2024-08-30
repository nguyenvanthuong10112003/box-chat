package com.box_chat.identity_service.controller;

import com.box_chat.identity_service.dto.request.AccountCreationRequest;
import com.box_chat.identity_service.dto.request.AccountVerifyRequest;
import com.box_chat.identity_service.dto.request.IntrospectRequest;
import com.box_chat.identity_service.dto.request.LoginRequest;
import com.box_chat.identity_service.dto.response.LoginResponse;
import com.box_chat.identity_service.dto.response.ResponseAPI;
import com.box_chat.identity_service.service.AccountService;
import com.box_chat.identity_service.service.UserRedisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {
    AccountService accountService;
    UserRedisService userRedisService;

    @PostMapping("/introspect")
    ResponseEntity<ResponseAPI<?>> authenticate(@RequestBody IntrospectRequest request) {
        if (accountService.introspect(request))
            return ResponseEntity.ok(ResponseAPI.success());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ResponseAPI.builder()
                .statusCode(9998)
                .message("Unauthenticated")
                .build());
    }

    @PostMapping("/create")
    public ResponseAPI<String> createAccount(@RequestBody @Valid AccountCreationRequest accountCreationRequest) {
        var response = accountService.createAccount(accountCreationRequest);
        userRedisService.deleteCache();
        return ResponseAPI.success(response);
    }

    @PostMapping("/login")
    public ResponseAPI<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseAPI.success(accountService.login(loginRequest));
    }

    @PostMapping("/resend")
    public ResponseAPI<?> resend(@RequestParam("token") @NotBlank(message = "Token is required") String token) {
        accountService.resend(token);
        return ResponseAPI.success();
    }

    @PostMapping("/verify")
    public ResponseAPI<?> verify(@RequestBody @Valid AccountVerifyRequest accountVerifyRequest) {
        accountService.verifyAccount(accountVerifyRequest.getToken(), accountVerifyRequest);
        return ResponseAPI.success();
    }
}
