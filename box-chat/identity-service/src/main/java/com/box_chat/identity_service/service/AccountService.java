package com.box_chat.identity_service.service;

import com.box_chat.event.dto.NotificationEvent;
import com.box_chat.identity_service.dto.request.AccountCreationRequest;
import com.box_chat.identity_service.dto.request.AccountVerifyRequest;
import com.box_chat.identity_service.dto.request.IntrospectRequest;
import com.box_chat.identity_service.dto.request.LoginRequest;
import com.box_chat.identity_service.dto.response.LoginResponse;
import com.box_chat.identity_service.entity.Account;
import com.box_chat.identity_service.entity.User;
import com.box_chat.identity_service.exception.AppException;
import com.box_chat.identity_service.exception.ErrorCode;
import com.box_chat.identity_service.mapper.AccountMapper;
import com.box_chat.identity_service.repository.AccountRepository;
import com.box_chat.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
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
    KafkaTemplate<String, Object> kafkaTemplate;

    public void resend(String token) {
        SignedJWT signedJWT = null;
        String userId = null;
        try {
            signedJWT = SignedJWT.parse(token);
            userId = signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Token invalid");
        }
        var user = userRepository.findById(userId).orElseThrow(() ->
            new RuntimeException("User is not exist"));
        var account = user.getAccount();
        String authCode = null;
        while ((authCode = createAuthCode()).equals(account.getAuthCode())) {}
        account.setAuthCode(authCode);
        account.setAuthTimeExpire(LocalDateTime.now().plusSeconds(VERIFY_VALID_DURATION));
        accountRepository.save(account);

        sendAuthenCode(user);
    }

    public boolean introspect(IntrospectRequest request)  {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (JOSEException | ParseException e) {
            isValid = false;
        }

        return isValid;
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(verifier))
            throw new RuntimeException("Token invalid");

        if (signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now()))
            throw new RuntimeException("Token expired");

        if (!userRepository.findById(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getAccount()
                .isVerified())
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFY);

        return signedJWT;
    }

    public void verifyAccount(String token, AccountVerifyRequest accountVerifyRequest) {
        SignedJWT signedJWT = null;
        String userId = null;
        try {
            signedJWT = SignedJWT.parse(token);
            userId = signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Token invalid");
        }
        var account = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User is not found"))
            .getAccount();

        if (!account.getAuthCode().equals(accountVerifyRequest.getAuthCode()))
            throw new RuntimeException("Authentication code is incorrect");

        if (account.getAuthTimeExpire().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Authentication code is expired");

        account.setVerified(true);
        accountRepository.save(account);
    }

    public String createAccount(AccountCreationRequest accountCreationRequest) {
        var account = accountMapper.toAccount(accountCreationRequest);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        String authCode = createAuthCode();
        account.setAuthCode(authCode);
        account.setAuthTimeExpire(LocalDateTime.now().plusSeconds(VERIFY_VALID_DURATION));
        try {
            account = accountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email is existed");
        }

        var newUser = User.builder().account(account).build();
        userRepository.save(newUser);

        sendAuthenCode(newUser);

        return jwtEncode(newUser);
    }

    private void sendAuthenCode(User user) {
        String nameNewUser = Strings.isBlank(user.getFullName()) ? "Người dùng mới" : user.getFullName();
        NotificationEvent notificationEvent = NotificationEvent
                .builder()
                .channel("EMAIL")
                .receiverEmail(user.getAccount().getEmail())
                .receiverName(nameNewUser)
                .subject("[BOXCHAT] Xác thực tài khoản")
                .htmlContent(String.format(
                    "</p>" +
                            "Xin chào %s. <br>" +
                            "Mã xác thực của bạn là: %s <br> " +
                            "Mã xác thực có hạn sử dụng %d phút. <br>" +
                            "Vui lòng không tiết lộ thông tin cho bất kỳ ai." +
                            "</p>", nameNewUser, user.getAccount().getAuthCode(), VERIFY_VALID_DURATION / 60))
                .build();
        try {
            kafkaTemplate.send("notification--send-email", notificationEvent);
        } catch (KafkaException kafkaException) {
            kafkaException.printStackTrace();
        }
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
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

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
            log.error(joseException.getMessage());
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
