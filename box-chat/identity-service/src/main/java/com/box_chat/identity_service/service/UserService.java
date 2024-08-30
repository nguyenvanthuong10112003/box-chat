package com.box_chat.identity_service.service;

import com.box_chat.identity_service.dto.request.UserUpdateRequest;
import com.box_chat.identity_service.dto.response.CustomPage;
import com.box_chat.identity_service.dto.response.UserResponse;
import com.box_chat.identity_service.entity.User;
import com.box_chat.identity_service.mapper.UserMapper;
import com.box_chat.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    public UserResponse mind() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userMapper.toUserResponse(
            userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User is not found")));
    }

    public UserResponse getByEmail(String email) {
        return userMapper.toUserResponse(
            userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User is not found")));
    }

    public UserResponse updateUser(UserUpdateRequest userUpdateRequest) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUser(user, userUpdateRequest);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public CustomPage<UserResponse> findAll(Pageable pageable, String keySearch) {
        Page<User> page = userRepository.findAllByMatchEmail(pageable, keySearch);
        return CustomPage.<UserResponse>builder()
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalPage(page.getTotalPages())
            .data(page.map(userMapper::toUserResponse).toList())
            .build();
    }
}
