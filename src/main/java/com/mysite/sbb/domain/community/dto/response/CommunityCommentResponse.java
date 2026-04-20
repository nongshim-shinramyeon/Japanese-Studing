package com.mysite.sbb.domain.community.dto.response;

import com.mysite.sbb.domain.community.entity.CommunityComment;
import java.time.LocalDateTime;

public record CommunityCommentResponse(
        Long id,
        Long postId,
        Long parentId,
        String authorName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommunityCommentResponse from(CommunityComment comment) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getAuthorName(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
