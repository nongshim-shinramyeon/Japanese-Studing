package com.mysite.sbb.domain.community.controller;

import com.mysite.sbb.domain.community.dto.request.CommunityCommentRequest;
import com.mysite.sbb.domain.community.dto.request.CommunityPostRequest;
import com.mysite.sbb.domain.community.dto.response.CommunityCommentResponse;
import com.mysite.sbb.domain.community.dto.response.CommunityPostResponse;
import com.mysite.sbb.domain.community.service.CommunityService;
import com.mysite.sbb.global.api.ApiResponse;
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
    public ResponseEntity<ApiResponse<CommunityPostResponse>> createPost(@Valid @RequestBody CommunityPostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(communityService.createPost(request)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<CommunityPostResponse>>> getPosts(
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getPosts(pageable)));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getPost(postId)));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody CommunityPostRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.updatePost(postId, request)));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        communityService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommunityCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(communityService.createComment(postId, request)));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommunityCommentResponse>>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getComments(postId, pageable)));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommunityCommentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityService.updateComment(commentId, request)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        communityService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
