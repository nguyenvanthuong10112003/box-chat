package com.box_chat.identity_service.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResponseAPI<T> {
    int statusCode;
    String message;
    T data;

    public static ResponseAPI<?> success() {
        return ResponseAPI.builder().statusCode(200).message("Success!").build();
    }

    public static <T> ResponseAPI<T> success(T data) {
        return ResponseAPI.<T>builder().statusCode(200).message("Success!").data(data).build();
    }
}
