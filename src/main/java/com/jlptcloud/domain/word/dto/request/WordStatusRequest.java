package com.jlptcloud.domain.word.dto.request;

import com.jlptcloud.domain.study.StudyStatus;
import jakarta.validation.constraints.NotNull;

public record WordStatusRequest(
        @NotNull(message = "Study status is required.")
        StudyStatus studyStatus
) {
}
