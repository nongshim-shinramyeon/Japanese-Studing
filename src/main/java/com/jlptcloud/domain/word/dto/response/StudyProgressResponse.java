package com.jlptcloud.domain.word.dto.response;

import java.util.List;

public record StudyProgressResponse(
        long totalWords,
        long dueReviews,
        long reviewNeeded,
        int completionRate,
        List<LevelProgressResponse> levels
) {
}
