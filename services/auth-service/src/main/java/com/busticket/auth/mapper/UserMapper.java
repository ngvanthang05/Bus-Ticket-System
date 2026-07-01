package com.xekhach.auth.mapper;

import com.xekhach.auth.dto.response.UserResponse;
import com.xekhach.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}