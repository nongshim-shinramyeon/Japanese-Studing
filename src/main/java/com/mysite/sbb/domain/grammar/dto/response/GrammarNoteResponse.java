package com.mysite.sbb.domain.grammar.dto.response;

import com.mysite.sbb.domain.grammar.entity.GrammarNote;
import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
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
