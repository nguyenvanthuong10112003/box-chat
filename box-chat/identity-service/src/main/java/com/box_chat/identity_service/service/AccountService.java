package com.box_chat.identity_service.service;

import com.box_chat.identity_service.dto.request.AccountCreationRequest;
import com.box_chat.identity_service.dto.request.AccountVerifyRequest;
import com.box_chat.identity_service.dto.request.LoginRequest;
import com.box_chat.identity_service.dto.response.AccountResponse;
import com.box_chat.identity_service.dto.response.LoginResponse;
import com.box_chat.identity_service.entity.Account;
import com.box_chat.identity_service.entity.User;
import com.box_chat.identity_service.mapper.AccountMapper;
import com.box_chat.identity_service.repository.AccountRepository;
import com.box_chat.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountService {
    @NonFinal
    @Value("${jwt.signer-key}")
    String SIGNER_KEY;
    @NonFinal
    @Value("${jwt.valid-duration}")
    int VALID_DURATION;
    @NonFinal
    @Value("${auth.verify.valid-duration}")
    int VERIFY_VALID_DURATION;
    AccountRepository accountRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AccountMapper accountMapper;
    Random random;

    public void verifyAccount(AccountVerifyRequest accountVerifyRequest) {
        var account = accountRepository.findById(accountVerifyRequest.getAccountId())
            .orElseThrow(() -> new RuntimeException("Account is not existed"));

        if (account.getAuthCode().equals(accountVerifyRequest.getAuthCode()))
            throw new RuntimeException("Authentication code is incorrect");

        if (account.getAuthTimeExpire().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Authentication code is expired");

        account.setVerified(true);
        accountRepository.save(account);
    }

    public String createAccount(AccountCreationRequest accountCreationRequest) {
        var account = accountMapper.toAccount(accountCreationRequest);

        try {
            account.setAuthCode(createAuthCode());
            account.setAuthTimeExpire(LocalDateTime.now().plusSeconds(VERIFY_VALID_DURATION));
            account = accountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email is existed");
        }

        var newUser = User.builder().account(account).build();
        userRepository.save(newUser);

        return jwtEncode(account);
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

        return LoginResponse.builder().token(jwtEncode(user.getAccount())).build();
    }

    String jwtEncode(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getId())
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

    String createAuthCode() {
        StringBuilder value = new StringBuilder();
        while (value.length() < 6)
            value.append(random.nextInt(10));
        return value.toString();
    }
}
