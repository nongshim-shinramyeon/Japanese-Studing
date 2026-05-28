package com.jlptcloud.domain.community.service;

import com.jlptcloud.domain.community.dto.request.CommunityCommentRequest;
import com.jlptcloud.domain.community.dto.request.CommunityPostRequest;
import com.jlptcloud.domain.community.dto.response.CommunityCommentResponse;
import com.jlptcloud.domain.community.dto.response.CommunityPostResponse;
import com.jlptcloud.domain.community.entity.CommunityComment;
import com.jlptcloud.domain.community.entity.CommunityPost;
import com.jlptcloud.domain.community.repository.CommunityCommentRepository;
import com.jlptcloud.domain.community.repository.CommunityPostRepository;
import com.jlptcloud.domain.user.entity.AppUser;
import com.jlptcloud.domain.user.service.AuthService;
import com.jlptcloud.global.exception.BusinessException;
import com.jlptcloud.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final AuthService authService;

    public CommunityService(CommunityPostRepository communityPostRepository,
                            CommunityCommentRepository communityCommentRepository,
                            AuthService authService) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.authService = authService;
    }

    @Transactional
    public CommunityPostResponse createPost(CommunityPostRequest request, Long userId) {
        AppUser user = authService.getUser(userId);
        CommunityPost post = new CommunityPost(
                user,
                request.authorName(),
                request.title(),
                request.content()
        );
        return CommunityPostResponse.from(communityPostRepository.save(post), userId);
    }

    public Page<CommunityPostResponse> getPosts(Pageable pageable, Long userId) {
        return communityPostRepository.findAll(pageable).map(post -> CommunityPostResponse.from(post, userId));
    }

    public CommunityPostResponse getPost(Long postId, Long userId) {
        return CommunityPostResponse.from(findPost(postId), userId);
    }

    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostRequest request, Long userId) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, userId);
        post.update(request.authorName(), request.title(), request.content());
        return CommunityPostResponse.from(post, userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = findPost(postId);
        validatePostOwner(post, userId);
        communityCommentRepository.deleteByPost(post);
        communityPostRepository.delete(post);
    }

    @Transactional
    public CommunityCommentResponse createComment(Long postId, CommunityCommentRequest request, Long userId) {
        CommunityPost post = findPost(postId);
        AppUser user = authService.getUser(userId);
        CommunityComment parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
            if (!parent.getPost().getId().equals(postId)) {
                throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
            }
        }
        CommunityComment comment = new CommunityComment(post, parent, user, request.authorName(), request.content());
        return CommunityCommentResponse.from(communityCommentRepository.save(comment), userId);
    }

    public Page<CommunityCommentResponse> getComments(Long postId, Pageable pageable, Long userId) {
        CommunityPost post = findPost(postId);
        return communityCommentRepository.findByPost(post, pageable).map(comment -> CommunityCommentResponse.from(comment, userId));
    }

    @Transactional
    public CommunityCommentResponse updateComment(Long commentId, CommunityCommentRequest request, Long userId) {
        CommunityComment comment = findComment(commentId);
        validateCommentOwner(comment, userId);
        comment.update(request.authorName(), request.content());
        return CommunityCommentResponse.from(comment, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = findComment(commentId);
        validateCommentOwner(comment, userId);
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

    private void validatePostOwner(CommunityPost post, Long userId) {
        if (userId == null || !post.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_FORBIDDEN);
        }
    }

    private void validateCommentOwner(CommunityComment comment, Long userId) {
        if (userId == null || !comment.isOwnedBy(userId)) {
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
