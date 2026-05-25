package com.jlptcloud.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityPostRequest(
        @NotBlank(message = "Author name is required.")
        @Size(max = 80, message = "Author name must be 80 characters or less.")
        String authorName,

        @NotBlank(message = "Title is required.")
        @Size(max = 120, message = "Title must be 120 characters or less.")
        String title,

        @NotBlank(message = "Content is required.")
        @Size(max = 3000, message = "Content must be 3000 characters or less.")
        String content
) {
}
