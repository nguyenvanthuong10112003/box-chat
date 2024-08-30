package com.box_chat.api_gateway.repository;

import com.box_chat.api_gateway.dto.request.IntrospectRequest;
import com.box_chat.api_gateway.dto.response.ResponseAPI;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseAPI<?>> introspect(@RequestBody @Valid IntrospectRequest request);
}