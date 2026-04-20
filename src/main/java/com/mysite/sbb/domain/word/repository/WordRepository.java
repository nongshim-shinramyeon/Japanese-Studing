package com.mysite.sbb.domain.word.repository;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import com.mysite.sbb.domain.word.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

    Page<Word> findByJlptLevelAndStudyStatus(JlptLevel jlptLevel, StudyStatus studyStatus, Pageable pageable);

    Page<Word> findByJlptLevel(JlptLevel jlptLevel, Pageable pageable);

    Page<Word> findByStudyStatus(StudyStatus studyStatus, Pageable pageable);
}
