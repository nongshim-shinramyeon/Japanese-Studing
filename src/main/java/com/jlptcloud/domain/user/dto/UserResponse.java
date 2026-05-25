package com.jlptcloud.domain.user.dto;

import com.jlptcloud.domain.user.entity.AppUser;

public record UserResponse(
        Long id,
        String username
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
