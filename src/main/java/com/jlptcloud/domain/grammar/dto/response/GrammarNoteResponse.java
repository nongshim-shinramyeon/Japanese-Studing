package com.jlptcloud.domain.grammar.dto.response;

import com.jlptcloud.domain.grammar.entity.GrammarNote;
import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import java.time.LocalDateTime;

public record GrammarNoteResponse(
        Long id,
        String title,
        String patternExpression,
        String meaning,
        String explanation,
        String exampleSentence,
        JlptLevel jlptLevel,
        StudyStatus studyStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GrammarNoteResponse from(GrammarNote grammarNote) {
        return new GrammarNoteResponse(
                grammarNote.getId(),
                grammarNote.getTitle(),
                grammarNote.getPatternExpression(),
                grammarNote.getMeaning(),
                grammarNote.getExplanation(),
                grammarNote.getExampleSentence(),
                grammarNote.getJlptLevel(),
                grammarNote.getStudyStatus(),
                grammarNote.getCreatedAt(),
                grammarNote.getUpdatedAt()
        );
    }
}
