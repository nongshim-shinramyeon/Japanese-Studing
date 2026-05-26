package com.jlptcloud.domain.word.dto.response;

import java.util.List;

public record StudyProgressResponse(
        long totalWords,
        long studiedWords,
        long dueReviews,
        long reviewNeeded,
        int completionRate,
        int averageMemoryScore,
        long stageSevenWords,
        List<LevelProgressResponse> levels
) {
}
