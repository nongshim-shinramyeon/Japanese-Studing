package com.jlptcloud.domain.word.controller;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.dto.request.ReviewAnswerRequest;
import com.jlptcloud.domain.word.dto.request.WordRequest;
import com.jlptcloud.domain.word.dto.request.WordStatusRequest;
import com.jlptcloud.domain.word.dto.response.StudyProgressResponse;
import com.jlptcloud.domain.word.dto.response.WordResponse;
import com.jlptcloud.domain.word.service.WordService;
import com.jlptcloud.domain.user.controller.AuthController;
import com.jlptcloud.global.api.ApiResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WordResponse>> create(@Valid @RequestBody WordRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(wordService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WordResponse>>> getWords(
            @RequestParam(required = false) JlptLevel jlptLevel,
            @RequestParam(required = false) StudyStatus studyStatus,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(wordService.getWords(jlptLevel, studyStatus, keyword, userId, pageable)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudyProgressResponse>> dashboard(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(wordService.getProgress(userId)));
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<Page<WordResponse>>> reviewWords(
            @RequestParam(required = false) JlptLevel jlptLevel,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 25) Pageable pageable,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(wordService.getReviewWords(jlptLevel, keyword, userId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> getWord(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(wordService.getWord(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> update(@PathVariable Long id, @Valid @RequestBody WordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(wordService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<WordResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody WordStatusRequest request,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(wordService.updateStatus(id, request, userId)));
    }

    @PatchMapping("/{id}/study")
    public ResponseEntity<ApiResponse<WordResponse>> markStudied(
            @PathVariable Long id,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(wordService.markStudied(id, userId)));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<WordResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAnswerRequest request,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(wordService.reviewWord(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        wordService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
