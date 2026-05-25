package com.jlptcloud.domain.community.service;

import com.jlptcloud.domain.community.dto.request.CommunityCommentRequest;
import com.jlptcloud.domain.community.dto.request.CommunityPostRequest;
import com.jlptcloud.domain.community.dto.response.CommunityCommentResponse;
import com.jlptcloud.domain.community.dto.response.CommunityPostResponse;
import com.jlptcloud.domain.community.entity.CommunityComment;
import com.jlptcloud.domain.community.entity.CommunityPost;
import com.jlptcloud.domain.community.repository.CommunityCommentRepository;
import com.jlptcloud.domain.community.repository.CommunityPostRepository;
import com.jlptcloud.global.exception.BusinessException;
import com.jlptcloud.global.exception.ErrorCode;
import java.util.List;
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
                requireOwnerKey(ownerKey)
        );
        return CommunityPostResponse.from(communityPostRepository.save(post), ownerKey);
    }

    public Page<CommunityPostResponse> getPosts(Pageable pageable, String ownerKey) {
        return communityPostRepository.findAll(pageable).map(post -> CommunityPostResponse.from(post, ownerKey));
    }

    public CommunityPostResponse getPost(Long postId, String ownerKey) {
        return CommunityPostResponse.from(findPost(postId), ownerKey);
    }

    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostRequest request, String ownerKey) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, ownerKey);
        post.update(request.authorName(), request.title(), request.content());
        return CommunityPostResponse.from(post, ownerKey);
    }

    @Transactional
    public void deletePost(Long postId, String ownerKey) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, ownerKey);
        communityCommentRepository.deleteByPost(post);
        communityPostRepository.delete(post);
    }

    @Transactional
    public CommunityCommentResponse createComment(Long postId, CommunityCommentRequest request, String ownerKey) {
        CommunityPost post = findPost(postId);
        CommunityComment parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
            if (!parent.getPost().getId().equals(postId)) {
                throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
            }
        }
        CommunityComment comment = new CommunityComment(post, parent, request.authorName(), request.content(), requireOwnerKey(ownerKey));
        return CommunityCommentResponse.from(communityCommentRepository.save(comment), ownerKey);
    }

    public Page<CommunityCommentResponse> getComments(Long postId, Pageable pageable, String ownerKey) {
        CommunityPost post = findPost(postId);
        return communityCommentRepository.findByPost(post, pageable).map(comment -> CommunityCommentResponse.from(comment, ownerKey));
    }

    @Transactional
    public CommunityCommentResponse updateComment(Long commentId, CommunityCommentRequest request, String ownerKey) {
        CommunityComment comment = findComment(commentId);
        validateCommentOwner(comment, ownerKey);
        comment.update(request.authorName(), request.content());
        return CommunityCommentResponse.from(comment, ownerKey);
    }

    @Transactional
    public void deleteComment(Long commentId, String ownerKey) {
        CommunityComment comment = findComment(commentId);
        validateCommentOwner(comment, ownerKey);
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

    private String requireOwnerKey(String ownerKey) {
        if (!StringUtils.hasText(ownerKey)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ownerKey.trim();
    }

    private void validatePostOwner(CommunityPost post, String ownerKey) {
        if (!StringUtils.hasText(ownerKey) || !post.isOwnedBy(ownerKey.trim())) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_FORBIDDEN);
        }
    }

    private void validateCommentOwner(CommunityComment comment, String ownerKey) {
        if (!StringUtils.hasText(ownerKey) || !comment.isOwnedBy(ownerKey.trim())) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_FORBIDDEN);
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
