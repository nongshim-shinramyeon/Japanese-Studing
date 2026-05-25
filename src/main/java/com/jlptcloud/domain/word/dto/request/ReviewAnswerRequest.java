package com.jlptcloud.domain.word.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReviewAnswerRequest(
        @NotNull(message = "Review result is required.")
        Boolean correct
) {
}
