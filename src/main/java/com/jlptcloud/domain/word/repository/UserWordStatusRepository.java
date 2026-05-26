package com.jlptcloud.domain.word.repository;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.entity.UserWordStatus;
import com.jlptcloud.domain.word.entity.Word;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWordStatusRepository extends JpaRepository<UserWordStatus, Long> {

    @EntityGraph(attributePaths = "word")
    Optional<UserWordStatus> findByUser_IdAndWord_Id(Long userId, Long wordId);

    @EntityGraph(attributePaths = "word")
    List<UserWordStatus> findByUser_IdAndWordIn(Long userId, Collection<Word> words);

    @EntityGraph(attributePaths = "word")
    List<UserWordStatus> findByUser_IdAndStudiedTrue(Long userId);

    long countByUser_IdAndStudyStatus(Long userId, StudyStatus studyStatus);

    long countByUser_IdAndStudiedTrue(Long userId);

    long countByUser_IdAndMemoryStage(Long userId, int memoryStage);

    long countByUser_IdAndWord_JlptLevel(Long userId, JlptLevel jlptLevel);

    long countByUser_IdAndWord_JlptLevelAndStudiedTrue(Long userId, JlptLevel jlptLevel);

    long countByUser_IdAndWord_JlptLevelAndMemoryStage(Long userId, JlptLevel jlptLevel, int memoryStage);

    long countByUser_IdAndWord_JlptLevelAndStudyStatus(
            Long userId,
            JlptLevel jlptLevel,
            StudyStatus studyStatus
    );

    long countByUser_IdAndNextReviewAtLessThanEqual(Long userId, LocalDateTime now);

    long countByUser_IdAndStudiedTrueAndNextReviewAtLessThanEqual(Long userId, LocalDateTime now);
}
