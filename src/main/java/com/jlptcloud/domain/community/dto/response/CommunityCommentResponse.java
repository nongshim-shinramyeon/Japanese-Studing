package com.jlptcloud.domain.community.dto.response;

import com.jlptcloud.domain.community.entity.CommunityComment;
import java.time.LocalDateTime;

public record CommunityCommentResponse(
        Long id,
        Long postId,
        Long parentId,
        String authorName,
        String content,
        boolean ownedByCurrentUser,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommunityCommentResponse from(CommunityComment comment) {
        return from(comment, null);
    }

    public static CommunityCommentResponse from(CommunityComment comment, String currentOwnerKey) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getAuthorName(),
                comment.getContent(),
                currentOwnerKey != null && comment.isOwnedBy(currentOwnerKey),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
