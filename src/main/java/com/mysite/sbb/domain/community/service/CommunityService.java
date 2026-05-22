package com.mysite.sbb.domain.community.service;

import com.mysite.sbb.domain.community.dto.request.CommunityCommentRequest;
import com.mysite.sbb.domain.community.dto.request.CommunityPostRequest;
import com.mysite.sbb.domain.community.dto.response.CommunityCommentResponse;
import com.mysite.sbb.domain.community.dto.response.CommunityPostResponse;
import com.mysite.sbb.domain.community.entity.CommunityComment;
import com.mysite.sbb.domain.community.entity.CommunityPost;
import com.mysite.sbb.domain.community.repository.CommunityCommentRepository;
import com.mysite.sbb.domain.community.repository.CommunityPostRepository;
import com.mysite.sbb.global.exception.BusinessException;
import com.mysite.sbb.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;

    public CommunityService(CommunityPostRepository communityPostRepository,
                            CommunityCommentRepository communityCommentRepository) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
    }

    @Transactional
    public CommunityPostResponse createPost(CommunityPostRequest request, String ownerKey) {
        CommunityPost post = new CommunityPost(
                request.authorName(),
                request.title(),
                request.content(),
                normalizeOwnerKey(ownerKey)
        );
        return CommunityPostResponse.from(communityPostRepository.save(post));
    }

    public Page<CommunityPostResponse> getPosts(Pageable pageable) {
        return communityPostRepository.findAll(pageable).map(CommunityPostResponse::from);
    }

    public CommunityPostResponse getPost(Long postId) {
        return CommunityPostResponse.from(findPost(postId));
    }

    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostRequest request, String ownerKey) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, ownerKey);
        post.update(request.authorName(), request.title(), request.content());
        return CommunityPostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, String ownerKey) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, ownerKey);
        communityCommentRepository.deleteByPost(post);
        communityPostRepository.delete(post);
    }

    @Transactional
    public CommunityCommentResponse createComment(Long postId, CommunityCommentRequest request) {
        CommunityPost post = findPost(postId);
        CommunityComment parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
            if (!parent.getPost().getId().equals(postId)) {
                throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
            }
        }
        CommunityComment comment = new CommunityComment(post, parent, request.authorName(), request.content());
        return CommunityCommentResponse.from(communityCommentRepository.save(comment));
    }

    public Page<CommunityCommentResponse> getComments(Long postId, Pageable pageable) {
        CommunityPost post = findPost(postId);
        return communityCommentRepository.findByPost(post, pageable).map(CommunityCommentResponse::from);
    }

    @Transactional
    public CommunityCommentResponse updateComment(Long commentId, CommunityCommentRequest request) {
        CommunityComment comment = findComment(commentId);
        comment.update(request.authorName(), request.content());
        return CommunityCommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        CommunityComment comment = findComment(commentId);
        deleteCommentTree(comment);
    }

    private CommunityPost findPost(Long postId) {
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
    }

    private CommunityComment findComment(Long commentId) {
        return communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
    }

    private String normalizeOwnerKey(String ownerKey) {
        if (StringUtils.hasText(ownerKey)) {
            return ownerKey.trim();
        }
        return UUID.randomUUID().toString();
    }

    private void validatePostOwner(CommunityPost post, String ownerKey) {
        if (!StringUtils.hasText(ownerKey) || !post.isOwnedBy(ownerKey.trim())) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_FORBIDDEN);
        }
    }

    private void deleteCommentTree(CommunityComment comment) {
        List<CommunityComment> replies = communityCommentRepository.findByParent(comment);
        for (CommunityComment reply : replies) {
            deleteCommentTree(reply);
        }
        communityCommentRepository.delete(comment);
    }
}
