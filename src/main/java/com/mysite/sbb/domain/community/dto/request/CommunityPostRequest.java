package com.mysite.sbb.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityPostRequest(
        @NotBlank(message = "작성자는 필수입니다.")
        @Size(max = 80, message = "작성자는 80자 이하여야 합니다.")
        String authorName,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 120, message = "제목은 120자 이하여야 합니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 3000, message = "본문은 3000자 이하여야 합니다.")
        String content
) {
}
