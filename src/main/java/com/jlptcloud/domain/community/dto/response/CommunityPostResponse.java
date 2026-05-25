package com.jlptcloud.domain.community.dto.response;

import com.jlptcloud.domain.community.entity.CommunityPost;
import java.time.LocalDateTime;

public record CommunityPostResponse(
        Long id,
        String authorName,
        String title,
        String content,
        boolean ownedByCurrentUser,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommunityPostResponse from(CommunityPost post) {
        return from(post, null);
    }

    public static CommunityPostResponse from(CommunityPost post, String currentOwnerKey) {
        return new CommunityPostResponse(
                post.getId(),
                post.getAuthorName(),
                post.getTitle(),
                post.getContent(),
                currentOwnerKey != null && post.isOwnedBy(currentOwnerKey),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
