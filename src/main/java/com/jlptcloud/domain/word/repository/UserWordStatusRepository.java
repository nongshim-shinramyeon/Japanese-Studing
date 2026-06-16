package com.jlptcloud.domain.word.repository;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.entity.UserWordStatus;
import com.jlptcloud.domain.word.entity.Word;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserWordStatusRepository extends JpaRepository<UserWordStatus, Long> {

    String CURRENT_MEMORY_SCORE_ORDER = """
            case
                when uws.lastReviewedAt is null or uws.lastReviewedAt > :now then uws.memoryScore
                else uws.memoryScore * power(0.7, (timestampdiff(minute, uws.lastReviewedAt, :now) / 1440.0) /
                    case uws.memoryStage
                        when 1 then 1.0
                        when 2 then 4.0
                        when 3 then 7.0
                        when 4 then 14.0
                        when 5 then 30.0
                        when 6 then 60.0
                        else 90.0
                    end)
            end asc, uws.nextReviewAt asc, w.id asc
            """;

    @EntityGraph(attributePaths = "word")
    Optional<UserWordStatus> findByUser_IdAndWord_Id(Long userId, Long wordId);

    @EntityGraph(attributePaths = "word")
    List<UserWordStatus> findByUser_IdAndWordIn(Long userId, Collection<Word> words);

    @EntityGraph(attributePaths = "word")
    List<UserWordStatus> findByUser_IdAndStudiedTrue(Long userId);

    @Query(
            value = """
                    select uws from UserWordStatus uws
                    join fetch uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                    order by """ + " " + CURRENT_MEMORY_SCORE_ORDER,
            countQuery = """
                    select count(uws) from UserWordStatus uws
                    where uws.user.id = :userId
                      and uws.studied = true
                    """
    )
    Page<UserWordStatus> findReviewPage(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query(
            value = """
                    select uws from UserWordStatus uws
                    join fetch uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                      and w.jlptLevel = :jlptLevel
                    order by """ + " " + CURRENT_MEMORY_SCORE_ORDER,
            countQuery = """
                    select count(uws) from UserWordStatus uws
                    join uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                      and w.jlptLevel = :jlptLevel
                    """
    )
    Page<UserWordStatus> findReviewPageByLevel(
            @Param("userId") Long userId,
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query(
            value = """
                    select uws from UserWordStatus uws
                    join fetch uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                      and (lower(w.japanese) like lower(concat('%', :keyword, '%'))
                           or lower(w.reading) like lower(concat('%', :keyword, '%'))
                           or lower(w.meaning) like lower(concat('%', :keyword, '%')))
                    order by """ + " " + CURRENT_MEMORY_SCORE_ORDER,
            countQuery = """
                    select count(uws) from UserWordStatus uws
                    join uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                      and (lower(w.japanese) like lower(concat('%', :keyword, '%'))
                           or lower(w.reading) like lower(concat('%', :keyword, '%'))
                           or lower(w.meaning) like lower(concat('%', :keyword, '%')))
                    """
    )
    Page<UserWordStatus> findReviewPageByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query(
            value = """
                    select uws from UserWordStatus uws
                    join fetch uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                      and w.jlptLevel = :jlptLevel
                      and (lower(w.japanese) like lower(concat('%', :keyword, '%'))
                           or lower(w.reading) like lower(concat('%', :keyword, '%'))
                           or lower(w.meaning) like lower(concat('%', :keyword, '%')))
                    order by """ + " " + CURRENT_MEMORY_SCORE_ORDER,
            countQuery = """
                    select count(uws) from UserWordStatus uws
                    join uws.word w
                    where uws.user.id = :userId
                      and uws.studied = true
                      and w.jlptLevel = :jlptLevel
                      and (lower(w.japanese) like lower(concat('%', :keyword, '%'))
                           or lower(w.reading) like lower(concat('%', :keyword, '%'))
                           or lower(w.meaning) like lower(concat('%', :keyword, '%')))
                    """
    )
    Page<UserWordStatus> findReviewPageByLevelAndKeyword(
            @Param("userId") Long userId,
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("keyword") String keyword,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

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
