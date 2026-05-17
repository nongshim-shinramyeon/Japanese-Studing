package com.mysite.sbb.domain.word.repository;

import com.mysite.sbb.domain.study.StudyStatus;
import com.mysite.sbb.domain.word.entity.UserWordStatus;
import com.mysite.sbb.domain.word.entity.Word;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWordStatusRepository extends JpaRepository<UserWordStatus, Long> {

    Optional<UserWordStatus> findByUser_IdAndWord_Id(Long userId, Long wordId);

    List<UserWordStatus> findByUser_IdAndWordIn(Long userId, Collection<Word> words);

    long countByUser_IdAndStudyStatus(Long userId, StudyStatus studyStatus);
}
