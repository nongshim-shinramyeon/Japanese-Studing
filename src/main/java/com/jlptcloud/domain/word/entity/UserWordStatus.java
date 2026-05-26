package com.jlptcloud.domain.word.entity;

import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.user.entity.AppUser;
import com.jlptcloud.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_word_status_user_word", columnNames = {"user_id", "word_id"})
})
public class UserWordStatus extends BaseTimeEntity {
    public static final double MEMORY_DECAY_RATE = 0.7;
    public static final int MIN_MEMORY_STAGE = 1;
    public static final int MAX_MEMORY_STAGE = 7;
    public static final double INITIAL_MEMORY_SCORE = 100.0;

    private static final int[] REVIEW_INTERVAL_DAYS = {1, 4, 7, 14, 30, 60, 90};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus studyStatus;

    @Column(nullable = false)
    private boolean studied;

    @Column(nullable = false)
    private int memoryStage = MIN_MEMORY_STAGE;

    @Column(nullable = false)
    private double memoryScore;

    @Column(nullable = false)
    private int correctStreak;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    private int wrongCount;

    @Column(nullable = false)
    private int reviewCount;

    private LocalDateTime nextReviewAt;

    private LocalDateTime lastReviewedAt;

    protected UserWordStatus() {
    }

    public UserWordStatus(AppUser user, Word word, StudyStatus studyStatus) {
        this.user = user;
        this.word = word;
        this.studyStatus = studyStatus;
        this.memoryStage = MIN_MEMORY_STAGE;
    }

    public Word getWord() {
        return word;
    }

    public StudyStatus getStudyStatus() {
        return studyStatus;
    }

    public boolean isStudied() {
        return studied;
    }

    public int getMemoryStage() {
        return memoryStage;
    }

    public double getMemoryScore() {
        return memoryScore;
    }

    public int getCorrectStreak() {
        return correctStreak;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void updateStatus(StudyStatus studyStatus) {
        this.studyStatus = studyStatus;
    }

    public void markStudied(LocalDateTime studiedAt) {
        if (studied) {
            return;
        }

        this.studied = true;
        this.studyStatus = StudyStatus.LEARNING;
        this.memoryStage = MIN_MEMORY_STAGE;
        this.memoryScore = INITIAL_MEMORY_SCORE;
        this.lastReviewedAt = studiedAt;
        this.nextReviewAt = studiedAt.plusDays(reviewIntervalDays());
    }

    public void recordReview(boolean correct, LocalDateTime reviewedAt) {
        if (!studied) {
            markStudied(reviewedAt);
        }

        double currentScore = currentMemoryScore(reviewedAt);
        this.lastReviewedAt = reviewedAt;
        this.reviewCount++;

        if (correct) {
            this.correctCount++;
            this.correctStreak++;
            this.memoryStage = Math.min(MAX_MEMORY_STAGE, this.memoryStage + 1);
            this.memoryScore = clampScore(currentScore + 20);
            this.studyStatus = this.memoryStage >= 6 ? StudyStatus.MASTERED : StudyStatus.LEARNING;
            this.nextReviewAt = reviewedAt.plusDays(reviewIntervalDays());
            return;
        }

        this.correctStreak = 0;
        this.wrongCount++;
        this.memoryStage = Math.max(MIN_MEMORY_STAGE, this.memoryStage - 2);
        this.memoryScore = clampScore(currentScore - 30);
        this.studyStatus = StudyStatus.REVIEW_NEEDED;
        this.nextReviewAt = reviewedAt.plusDays(1);
    }

    public int reviewIntervalDays() {
        return REVIEW_INTERVAL_DAYS[Math.max(MIN_MEMORY_STAGE, Math.min(MAX_MEMORY_STAGE, memoryStage)) - 1];
    }

    public double currentMemoryScore(LocalDateTime now) {
        if (!studied) {
            return 0.0;
        }
        if (lastReviewedAt == null) {
            return clampScore(memoryScore);
        }

        double elapsedDays = Math.max(0.0, Duration.between(lastReviewedAt, now).toMinutes() / 1440.0);
        double decayedScore = memoryScore * Math.pow(MEMORY_DECAY_RATE, elapsedDays / reviewIntervalDays());
        return clampScore(decayedScore);
    }

    public boolean isDueForReview(LocalDateTime now) {
        return studied && nextReviewAt != null && !nextReviewAt.isAfter(now);
    }

    private double clampScore(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }
}
