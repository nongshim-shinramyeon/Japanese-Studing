package com.jlptcloud.domain.word.repository;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.entity.Word;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, Long> {

    Page<Word> findByJlptLevelAndStudyStatus(JlptLevel jlptLevel, StudyStatus studyStatus, Pageable pageable);

    Page<Word> findByJlptLevel(JlptLevel jlptLevel, Pageable pageable);

    Page<Word> findByStudyStatus(StudyStatus studyStatus, Pageable pageable);

    List<Word> findAllByJlptLevel(JlptLevel jlptLevel, Sort sort);

    long countByJlptLevel(JlptLevel jlptLevel);

    @Query("""
            select w from Word w
            where (:jlptLevel is null or w.jlptLevel = :jlptLevel)
              and (:studyStatus is null or w.studyStatus = :studyStatus)
              and (:keyword is null or lower(w.japanese) like lower(concat('%', :keyword, '%'))
                   or lower(w.reading) like lower(concat('%', :keyword, '%'))
                   or lower(w.meaning) like lower(concat('%', :keyword, '%')))
            """)
    Page<Word> search(
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("studyStatus") StudyStatus studyStatus,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            select w from Word w
            where (:jlptLevel is null or w.jlptLevel = :jlptLevel)
              and (:keyword is null or lower(w.japanese) like lower(concat('%', :keyword, '%'))
                   or lower(w.reading) like lower(concat('%', :keyword, '%'))
                   or lower(w.meaning) like lower(concat('%', :keyword, '%')))
            """)
    List<Word> searchScoped(
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("keyword") String keyword,
            Sort sort
    );
}
