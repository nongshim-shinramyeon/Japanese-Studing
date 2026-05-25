package com.jlptcloud.domain.word.dto.response;

import com.jlptcloud.domain.study.JlptLevel;

public record LevelProgressResponse(
        JlptLevel jlptLevel,
        long totalWords,
        long newCount,
        long learningCount,
        long reviewNeededCount,
        long masteredCount,
        int completionRate
) {
}
