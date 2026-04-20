package com.mysite.sbb.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "WORD_NOT_FOUND", "단어를 찾을 수 없습니다."),
    GRAMMAR_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "GRAMMAR_NOTE_NOT_FOUND", "문법 노트를 찾을 수 없습니다."),
    COMMUNITY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."),
    COMMUNITY_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
