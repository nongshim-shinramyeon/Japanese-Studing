package com.jlptcloud.domain.grammar.dto.request;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GrammarNoteRequest(
        @NotBlank(message = "Grammar note title is required.")
        @Size(max = 120, message = "Grammar note title must be 120 characters or less.")
        String title,

        @NotBlank(message = "Grammar pattern is required.")
        @Size(max = 120, message = "Grammar pattern must be 120 characters or less.")
        String patternExpression,

        @NotBlank(message = "Meaning is required.")
        @Size(max = 200, message = "Meaning must be 200 characters or less.")
        String meaning,

        @NotBlank(message = "Explanation is required.")
        @Size(max = 1000, message = "Explanation must be 1000 characters or less.")
        String explanation,

        @NotBlank(message = "Example sentence is required.")
        @Size(max = 500, message = "Example sentence must be 500 characters or less.")
        String exampleSentence,

        @NotNull(message = "JLPT level is required.")
        JlptLevel jlptLevel,

        @NotNull(message = "Study status is required.")
        StudyStatus studyStatus
) {
}
