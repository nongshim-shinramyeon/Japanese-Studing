package com.mysite.sbb.domain.community.dto.response;

import com.mysite.sbb.domain.community.entity.CommunityPost;
import java.time.LocalDateTime;

public record CommunityPostResponse(
        Long id,
        String authorName,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommunityPostResponse from(CommunityPost post) {
        return new CommunityPostResponse(
                post.getId(),
                post.getAuthorName(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
