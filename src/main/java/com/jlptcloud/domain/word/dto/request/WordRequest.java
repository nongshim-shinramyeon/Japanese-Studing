package com.jlptcloud.domain.word.dto.request;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WordRequest(
        @NotBlank(message = "Japanese word is required.")
        @Size(max = 100, message = "Japanese word must be 100 characters or less.")
        String japanese,

        @NotBlank(message = "Reading is required.")
        @Size(max = 100, message = "Reading must be 100 characters or less.")
        String reading,

        @NotBlank(message = "Meaning is required.")
        @Size(max = 150, message = "Meaning must be 150 characters or less.")
        String meaning,

        @NotBlank(message = "Part of speech is required.")
        @Size(max = 50, message = "Part of speech must be 50 characters or less.")
        String partOfSpeech,

        @NotBlank(message = "Example sentence is required.")
        @Size(max = 500, message = "Example sentence must be 500 characters or less.")
        String exampleSentence,

        @NotNull(message = "JLPT level is required.")
        JlptLevel jlptLevel,

        @NotNull(message = "Study status is required.")
        StudyStatus studyStatus
) {
}
