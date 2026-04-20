package com.mysite.sbb.domain.grammar.controller;

import com.mysite.sbb.domain.grammar.dto.request.GrammarNoteRequest;
import com.mysite.sbb.domain.grammar.dto.response.GrammarNoteResponse;
import com.mysite.sbb.domain.grammar.service.GrammarNoteService;
import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grammar-notes")
public class GrammarNoteController {

    private final GrammarNoteService grammarNoteService;

    public GrammarNoteController(GrammarNoteService grammarNoteService) {
        this.grammarNoteService = grammarNoteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GrammarNoteResponse>> create(@Valid @RequestBody GrammarNoteRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(grammarNoteService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GrammarNoteResponse>>> getGrammarNotes(
            @RequestParam(required = false) JlptLevel jlptLevel,
            @RequestParam(required = false) StudyStatus studyStatus,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(grammarNoteService.getGrammarNotes(jlptLevel, studyStatus, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GrammarNoteResponse>> getGrammarNote(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(grammarNoteService.getGrammarNote(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GrammarNoteResponse>> update(@PathVariable Long id, @Valid @RequestBody GrammarNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(grammarNoteService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        grammarNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
