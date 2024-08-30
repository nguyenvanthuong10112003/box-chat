package com.box_chat.identity_service.controller;

import com.box_chat.identity_service.dto.request.CustomPageRequest;
import com.box_chat.identity_service.dto.request.UserUpdateRequest;
import com.box_chat.identity_service.dto.response.CustomPage;
import com.box_chat.identity_service.dto.response.ResponseAPI;
import com.box_chat.identity_service.dto.response.UserResponse;
import com.box_chat.identity_service.service.UserRedisService;
import com.box_chat.identity_service.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;
    UserRedisService userRedisService;

    @GetMapping("/me")
    public ResponseAPI<UserResponse> mind() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse userResponse = null;
        try {
            userResponse = userRedisService.getMind(userId);
        } catch (JsonProcessingException e) {}
        if (userResponse == null) {
            log.error("from database");
            userResponse = userService.mind();
            try {
                userRedisService.saveMind(userResponse);
            } catch (JsonProcessingException e) {}
        }
        return ResponseAPI.success(userResponse);
    }

    @GetMapping("/find")
    public ResponseAPI<UserResponse> find(@RequestParam("email") @NotBlank @Email String email) {
        UserResponse userResponse = null;
        try {
            userResponse = userRedisService.getFindUser(email);
        } catch (JsonProcessingException e) {}
        if (userResponse == null) {
            log.error("from database");
            userResponse = userService.getByEmail(email);
            try {
                userRedisService.saveFindUser(userResponse);
            } catch (JsonProcessingException e) {}
        }
        return ResponseAPI.success(userResponse);
    }

    @GetMapping("")
    public ResponseAPI<CustomPage<UserResponse>> findAll(
            @RequestParam(value = "page_size", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "page_number", defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(value = "key_search", defaultValue = "") String keySearch) {
        CustomPageRequest request = CustomPageRequest.builder()
            .pageSize(pageSize)
            .pageNumber(pageNumber)
            .keySearch(keySearch)
            .build();
        PageRequest pageRequest = PageRequest.of(
            request.getPageNumber(),
            request.getPageSize(),
            Sort.by(Sort.Order.asc("fullName")));
        CustomPage<UserResponse> page = null;
        try {
            page = userRedisService.getPage(request);
        } catch (JsonProcessingException e) {}
        if (page == null) {
            log.error("database");
            page = userService.findAll(pageRequest, request.getKeySearch());
            try {
                userRedisService.savePage(request, page);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseAPI.success(page);
    }

    @PutMapping("/update")
    public ResponseAPI<?> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        var user = userService.updateUser(userUpdateRequest);
        userRedisService.deleteCache();
        userRedisService.deleteCacheFind(user.getEmail());
        userRedisService.deleteCacheMind(user.getId());
        return ResponseAPI.success();
    }
}
