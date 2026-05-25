package com.jlptcloud.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityCommentRequest(
        Long parentId,

        @NotBlank(message = "Comment author name is required.")
        @Size(max = 80, message = "Comment author name must be 80 characters or less.")
        String authorName,

        @NotBlank(message = "Comment content is required.")
        @Size(max = 1200, message = "Comment content must be 1200 characters or less.")
        String content
) {
}
