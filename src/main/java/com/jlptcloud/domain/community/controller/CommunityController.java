package com.jlptcloud.domain.community.controller;

import com.jlptcloud.domain.community.dto.request.CommunityCommentRequest;
import com.jlptcloud.domain.community.dto.request.CommunityPostRequest;
import com.jlptcloud.domain.community.dto.response.CommunityCommentResponse;
import com.jlptcloud.domain.community.dto.response.CommunityPostResponse;
import com.jlptcloud.domain.community.service.CommunityService;
import com.jlptcloud.domain.user.controller.AuthController;
import com.jlptcloud.global.api.ApiResponse;
import com.jlptcloud.global.exception.BusinessException;
import com.jlptcloud.global.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> createPost(
            @Valid @RequestBody CommunityPostRequest request,
            HttpSession session
    ) {
        Long userId = currentUserId(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(communityService.createPost(request, userId)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<CommunityPostResponse>>> getPosts(
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getPosts(pageable, optionalUserId(session))));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> getPost(@PathVariable Long postId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getPost(postId, optionalUserId(session))));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody CommunityPostRequest request,
            HttpSession session
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.updatePost(postId, request, currentUserId(session))));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            HttpSession session
    ) {
        communityService.deletePost(postId, currentUserId(session));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommunityCommentRequest request,
            HttpSession session
    ) {
        Long userId = currentUserId(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(communityService.createComment(postId, request, userId)));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommunityCommentResponse>>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            HttpSession session
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getComments(postId, pageable, optionalUserId(session))));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommunityCommentRequest request,
            HttpSession session
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.updateComment(commentId, request, currentUserId(session))));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId, HttpSession session) {
        communityService.deleteComment(commentId, currentUserId(session));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long optionalUserId(HttpSession session) {
        return (Long) session.getAttribute(AuthController.SESSION_USER_ID);
    }

    private Long currentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
