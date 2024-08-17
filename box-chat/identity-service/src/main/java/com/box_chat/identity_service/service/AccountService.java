package com.box_chat.identity_service.service;

import com.box_chat.identity_service.dto.request.LoginRequest;
import com.box_chat.identity_service.dto.response.LoginResponse;
import com.box_chat.identity_service.dto.response.ResponseAPI;
import com.box_chat.identity_service.entity.User;
import com.box_chat.identity_service.repository.AccountRepository;
import com.box_chat.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountService {
    @Value("${jwt.signer-key}")
    String SIGNER_KEY;
    @Value("${jwt.valid-duration}")
    int VALID_DURATION;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public void createAccount() {

    }

    public LoginResponse login(LoginRequest loginRequest) {
        var user = userRepository.findUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email is not exist"));
        try {
            if (!passwordEncoder.matches(loginRequest.getPassword(), Objects.requireNonNull(user.getAccount()).getPassword()))
                throw new RuntimeException("Password is incorrect");
        } catch (NullPointerException nullPointerException) {
            throw new RuntimeException("Error");
        }

        return LoginResponse.builder().token(jwtEncode(user)).build();
    }

    String jwtEncode(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException joseException) {
            throw new RuntimeException("Cannot generate token!");
        }
    }
}
