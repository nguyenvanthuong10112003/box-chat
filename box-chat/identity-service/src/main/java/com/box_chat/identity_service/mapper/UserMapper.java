package com.box_chat.identity_service.mapper;

import com.box_chat.identity_service.dto.request.UserUpdateRequest;
import com.box_chat.identity_service.dto.response.UserResponse;
import com.box_chat.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "email", expression = "java(user.getAccount().getEmail())")
    @Mapping(target = "timeJoined", expression = "java(user.getAccount().getTimeCreate())")
    @Mapping(target = "gender", expression = "java(user.getGender())")
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
