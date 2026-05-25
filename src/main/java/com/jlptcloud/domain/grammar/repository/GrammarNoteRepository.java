package com.jlptcloud.domain.grammar.repository;

import com.jlptcloud.domain.grammar.entity.GrammarNote;
import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarNoteRepository extends JpaRepository<GrammarNote, Long> {

    Page<GrammarNote> findByJlptLevelAndStudyStatus(JlptLevel jlptLevel, StudyStatus studyStatus, Pageable pageable);

    Page<GrammarNote> findByJlptLevel(JlptLevel jlptLevel, Pageable pageable);

    Page<GrammarNote> findByStudyStatus(StudyStatus studyStatus, Pageable pageable);
}
