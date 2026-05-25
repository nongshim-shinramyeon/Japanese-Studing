package com.jlptcloud.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "WORD_NOT_FOUND", "Word not found."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", "Username is already taken."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "Invalid username or password."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please log in first."),
    GRAMMAR_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "GRAMMAR_NOTE_NOT_FOUND", "Grammar note not found."),
    COMMUNITY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_POST_NOT_FOUND", "Community post not found."),
    COMMUNITY_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMUNITY_POST_FORBIDDEN", "Only the original writer can edit or delete this post."),
    COMMUNITY_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_COMMENT_NOT_FOUND", "Community comment not found."),
    COMMUNITY_COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMUNITY_COMMENT_FORBIDDEN", "Only the original writer can edit or delete this comment."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error.");

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
