package com.mysite.sbb.domain.word.dto.request;

import com.mysite.sbb.domain.study.StudyStatus;
import jakarta.validation.constraints.NotNull;

public record WordStatusRequest(
        @NotNull(message = "Study status is required.")
        StudyStatus studyStatus
) {
}
