package com.mysite.sbb.domain.word.dto.request;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WordRequest(
        @NotBlank(message = "일본어 단어는 필수입니다.")
        @Size(max = 100, message = "일본어 단어는 100자 이하여야 합니다.")
        String japanese,

        @NotBlank(message = "읽는 법은 필수입니다.")
        @Size(max = 100, message = "읽는 법은 100자 이하여야 합니다.")
        String reading,

        @NotBlank(message = "뜻은 필수입니다.")
        @Size(max = 150, message = "뜻은 150자 이하여야 합니다.")
        String meaning,

        @NotBlank(message = "품사는 필수입니다.")
        @Size(max = 50, message = "품사는 50자 이하여야 합니다.")
        String partOfSpeech,

        @NotBlank(message = "예문은 필수입니다.")
        @Size(max = 500, message = "예문은 500자 이하여야 합니다.")
        String exampleSentence,

        @NotNull(message = "JLPT 레벨은 필수입니다.")
        JlptLevel jlptLevel,

        @NotNull(message = "학습 상태는 필수입니다.")
        StudyStatus studyStatus
) {
}
