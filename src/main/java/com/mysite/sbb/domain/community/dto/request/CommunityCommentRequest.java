package com.mysite.sbb.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityCommentRequest(
        Long parentId,

        @NotBlank(message = "댓글 작성자는 필수입니다.")
        @Size(max = 80, message = "댓글 작성자는 80자 이하여야 합니다.")
        String authorName,

        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1200, message = "댓글 내용은 1200자 이하여야 합니다.")
        String content
) {
}
