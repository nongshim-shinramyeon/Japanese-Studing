package com.mysite.sbb.domain.word.dto.response;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import com.mysite.sbb.domain.word.entity.Word;
import java.time.LocalDateTime;

public record WordResponse(
        Long id,
        String japanese,
        String reading,
        String meaning,
        String partOfSpeech,
        String exampleSentence,
        JlptLevel jlptLevel,
        StudyStatus studyStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WordResponse from(Word word) {
        return new WordResponse(
                word.getId(),
                word.getJapanese(),
                word.getReading(),
                word.getMeaning(),
                word.getPartOfSpeech(),
                word.getExampleSentence(),
                word.getJlptLevel(),
                word.getStudyStatus(),
                word.getCreatedAt(),
                word.getUpdatedAt()
        );
    }
}
