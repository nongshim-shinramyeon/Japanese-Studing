package com.mysite.sbb.domain.word.service;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import com.mysite.sbb.domain.word.dto.request.WordRequest;
import com.mysite.sbb.domain.word.dto.request.WordStatusRequest;
import com.mysite.sbb.domain.word.dto.response.WordResponse;
import com.mysite.sbb.domain.user.entity.AppUser;
import com.mysite.sbb.domain.user.service.AuthService;
import com.mysite.sbb.domain.word.entity.UserWordStatus;
import com.mysite.sbb.domain.word.entity.Word;
import com.mysite.sbb.domain.word.repository.UserWordStatusRepository;
import com.mysite.sbb.domain.word.repository.WordRepository;
import com.mysite.sbb.global.exception.BusinessException;
import com.mysite.sbb.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WordService {

    private final WordRepository wordRepository;
    private final UserWordStatusRepository userWordStatusRepository;
    private final AuthService authService;

    public WordService(WordRepository wordRepository, UserWordStatusRepository userWordStatusRepository,
                       AuthService authService) {
        this.wordRepository = wordRepository;
        this.userWordStatusRepository = userWordStatusRepository;
        this.authService = authService;
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

    public Page<WordResponse> getWords(JlptLevel jlptLevel, StudyStatus studyStatus, Long userId, Pageable pageable) {
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
        return toUserWordResponses(words, userId);
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

    @Transactional
    public WordResponse updateStatus(Long id, WordStatusRequest request, Long userId) {
        AppUser user = authService.getUser(userId);
        Word word = findWord(id);
        UserWordStatus userWordStatus = userWordStatusRepository.findByUser_IdAndWord_Id(user.getId(), word.getId())
                .orElseGet(() -> new UserWordStatus(user, word, StudyStatus.NEW));

        userWordStatus.updateStatus(request.studyStatus());
        userWordStatusRepository.save(userWordStatus);
        return WordResponse.from(word, userWordStatus.getStudyStatus());
    }

    private Page<WordResponse> toUserWordResponses(Page<Word> words, Long userId) {
        if (userId == null || words.isEmpty()) {
            return words.map(WordResponse::from);
        }

        List<Word> wordContent = words.getContent();
        Map<Long, UserWordStatus> statuses = userWordStatusRepository.findByUser_IdAndWordIn(userId, wordContent)
                .stream()
                .collect(Collectors.toMap(status -> status.getWord().getId(), Function.identity()));

        List<WordResponse> responses = wordContent.stream()
                .map(word -> WordResponse.from(word, getUserStatus(statuses, word)))
                .toList();
        return new PageImpl<>(responses, words.getPageable(), words.getTotalElements());
    }

    private StudyStatus getUserStatus(Map<Long, UserWordStatus> statuses, Word word) {
        UserWordStatus userWordStatus = statuses.get(word.getId());
        if (userWordStatus == null) {
            return StudyStatus.NEW;
        }
        return userWordStatus.getStudyStatus();
    }

    private Word findWord(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORD_NOT_FOUND));
    }
}
