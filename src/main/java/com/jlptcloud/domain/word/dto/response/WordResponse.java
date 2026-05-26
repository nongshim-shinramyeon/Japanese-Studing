package com.jlptcloud.domain.word.dto.response;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.entity.UserWordStatus;
import com.jlptcloud.domain.word.entity.Word;
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
        boolean studied,
        Integer memoryStage,
        Double memoryScore,
        Double currentMemoryScore,
        Integer reviewIntervalDays,
        Boolean dueForReview,
        Integer correctStreak,
        Integer correctCount,
        Integer wrongCount,
        Integer reviewCount,
        LocalDateTime nextReviewAt,
        LocalDateTime lastReviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WordResponse from(Word word) {
        return from(word, word.getStudyStatus());
    }

    public static WordResponse from(Word word, StudyStatus studyStatus) {
        return new WordResponse(
                word.getId(),
                word.getJapanese(),
                word.getReading(),
                word.getMeaning(),
                word.getPartOfSpeech(),
                word.getExampleSentence(),
                word.getJlptLevel(),
                studyStatus,
                false,
                0,
                0.0,
                0.0,
                0,
                false,
                0,
                0,
                0,
                0,
                null,
                null,
                word.getCreatedAt(),
                word.getUpdatedAt()
        );
    }

    public static WordResponse from(Word word, UserWordStatus userWordStatus) {
        return from(word, userWordStatus, LocalDateTime.now());
    }

    public static WordResponse from(Word word, UserWordStatus userWordStatus, LocalDateTime now) {
        if (userWordStatus == null) {
            return from(word, StudyStatus.NEW);
        }

        return new WordResponse(
                word.getId(),
                word.getJapanese(),
                word.getReading(),
                word.getMeaning(),
                word.getPartOfSpeech(),
                word.getExampleSentence(),
                word.getJlptLevel(),
                userWordStatus.getStudyStatus(),
                userWordStatus.isStudied(),
                userWordStatus.getMemoryStage(),
                roundScore(userWordStatus.getMemoryScore()),
                roundScore(userWordStatus.currentMemoryScore(now)),
                userWordStatus.reviewIntervalDays(),
                userWordStatus.isDueForReview(now),
                userWordStatus.getCorrectStreak(),
                userWordStatus.getCorrectCount(),
                userWordStatus.getWrongCount(),
                userWordStatus.getReviewCount(),
                userWordStatus.getNextReviewAt(),
                userWordStatus.getLastReviewedAt(),
                word.getCreatedAt(),
                word.getUpdatedAt()
        );
    }

    private static double roundScore(double score) {
        return Math.round(score * 10.0) / 10.0;
    }
}
