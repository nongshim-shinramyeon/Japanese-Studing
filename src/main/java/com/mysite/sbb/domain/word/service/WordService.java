package com.mysite.sbb.domain.word.service;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import com.mysite.sbb.domain.word.dto.request.WordRequest;
import com.mysite.sbb.domain.word.dto.response.WordResponse;
import com.mysite.sbb.domain.word.entity.Word;
import com.mysite.sbb.domain.word.repository.WordRepository;
import com.mysite.sbb.global.exception.BusinessException;
import com.mysite.sbb.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WordService {

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Transactional
    public WordResponse create(WordRequest request) {
        Word word = new Word(
                request.japanese(),
                request.reading(),
                request.meaning(),
                request.partOfSpeech(),
                request.exampleSentence(),
                request.jlptLevel(),
                request.studyStatus()
        );
        return WordResponse.from(wordRepository.save(word));
    }

    public Page<WordResponse> getWords(JlptLevel jlptLevel, StudyStatus studyStatus, Pageable pageable) {
        Page<Word> words;
        if (jlptLevel != null && studyStatus != null) {
            words = wordRepository.findByJlptLevelAndStudyStatus(jlptLevel, studyStatus, pageable);
        } else if (jlptLevel != null) {
            words = wordRepository.findByJlptLevel(jlptLevel, pageable);
        } else if (studyStatus != null) {
            words = wordRepository.findByStudyStatus(studyStatus, pageable);
        } else {
            words = wordRepository.findAll(pageable);
        }
        return words.map(WordResponse::from);
    }

    public WordResponse getWord(Long id) {
        return WordResponse.from(findWord(id));
    }

    @Transactional
    public WordResponse update(Long id, WordRequest request) {
        Word word = findWord(id);
        word.update(
                request.japanese(),
                request.reading(),
                request.meaning(),
                request.partOfSpeech(),
                request.exampleSentence(),
                request.jlptLevel(),
                request.studyStatus()
        );
        return WordResponse.from(word);
    }

    @Transactional
    public void delete(Long id) {
        wordRepository.delete(findWord(id));
    }

    private Word findWord(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORD_NOT_FOUND));
    }
}
