package com.box_chat.api_gateway.configuration;

import com.box_chat.api_gateway.dto.request.IntrospectRequest;
import com.box_chat.api_gateway.dto.response.ResponseAPI;
import com.box_chat.api_gateway.repository.IdentityClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {
    ObjectMapper objectMapper;
    IdentityClient identityClient;

    @NonFinal
    String[] publicEndpoints = {
        "/identity/auth/login", "/identity/auth/create", "/identity/auth/verify", "/identity/auth/resend"
    };

    @NonFinal
    @Value("${app.api-prefix}")
    String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");

        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse(), null);

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        return identityClient.introspect(IntrospectRequest.builder()
            .token(token).build()).flatMap(introspectResponse -> chain.filter(exchange))
                .onErrorResume(throwable -> unauthenticated(exchange.getResponse(), throwable));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request){
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    Mono<Void> unauthenticated(ServerHttpResponse response, Throwable throwable) {
        ResponseAPI<?> apiResponse = extractResponseAPI(throwable);
        if (apiResponse == null)
            apiResponse = ResponseAPI.builder()
                .statusCode(9998)
                .message("Unauthenticated")
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private ResponseAPI<?> extractResponseAPI(Throwable throwable) {
        // Giả sử throwable chứa thông tin lỗi dưới dạng WebClientResponseException
        if (throwable instanceof WebClientResponseException webClientResponseException) {
            try {
                return objectMapper.readValue(webClientResponseException.getResponseBodyAsString(), ResponseAPI.class);
            } catch (IOException e) {
                log.error("Failed to parse error response: {}", e.getMessage());
            }
        }
        return null;
    }
}
