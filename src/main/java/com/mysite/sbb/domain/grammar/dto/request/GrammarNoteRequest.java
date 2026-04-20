package com.mysite.sbb.domain.grammar.dto.request;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GrammarNoteRequest(
        @NotBlank(message = "문법 노트 제목은 필수입니다.")
        @Size(max = 120, message = "문법 노트 제목은 120자 이하여야 합니다.")
        String title,

        @NotBlank(message = "문형은 필수입니다.")
        @Size(max = 120, message = "문형은 120자 이하여야 합니다.")
        String patternExpression,

        @NotBlank(message = "의미는 필수입니다.")
        @Size(max = 200, message = "의미는 200자 이하여야 합니다.")
        String meaning,

        @NotBlank(message = "설명은 필수입니다.")
        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
        String explanation,

        @NotBlank(message = "예문은 필수입니다.")
        @Size(max = 500, message = "예문은 500자 이하여야 합니다.")
        String exampleSentence,

        @NotNull(message = "JLPT 레벨은 필수입니다.")
        JlptLevel jlptLevel,

        @NotNull(message = "학습 상태는 필수입니다.")
        StudyStatus studyStatus
) {
}
