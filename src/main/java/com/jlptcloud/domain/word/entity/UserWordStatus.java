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
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_word_status_user_word", columnNames = {"user_id", "word_id"})
})
public class UserWordStatus extends BaseTimeEntity {

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
    private int correctStreak;

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
    }

    public Word getWord() {
        return word;
    }

    public StudyStatus getStudyStatus() {
        return studyStatus;
    }

    public int getCorrectStreak() {
        return correctStreak;
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

    public void recordReview(boolean correct, LocalDateTime reviewedAt) {
        this.lastReviewedAt = reviewedAt;
        this.reviewCount++;

        if (correct) {
            this.correctStreak++;
            if (correctStreak >= 3) {
                this.studyStatus = StudyStatus.MASTERED;
                this.nextReviewAt = reviewedAt.plusDays(14);
            } else if (correctStreak == 2) {
                this.studyStatus = StudyStatus.LEARNING;
                this.nextReviewAt = reviewedAt.plusDays(3);
            } else {
                this.studyStatus = StudyStatus.LEARNING;
                this.nextReviewAt = reviewedAt.plusDays(1);
            }
            return;
        }

        this.correctStreak = 0;
        this.wrongCount++;
        this.studyStatus = StudyStatus.REVIEW_NEEDED;
        this.nextReviewAt = reviewedAt.plusHours(12);
    }
}
