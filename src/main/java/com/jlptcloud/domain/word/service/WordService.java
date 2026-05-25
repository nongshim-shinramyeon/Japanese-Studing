package com.jlptcloud.domain.word.service;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.dto.request.ReviewAnswerRequest;
import com.jlptcloud.domain.word.dto.request.WordRequest;
import com.jlptcloud.domain.word.dto.request.WordStatusRequest;
import com.jlptcloud.domain.word.dto.response.LevelProgressResponse;
import com.jlptcloud.domain.word.dto.response.StudyProgressResponse;
import com.jlptcloud.domain.word.dto.response.WordResponse;
import com.jlptcloud.domain.user.entity.AppUser;
import com.jlptcloud.domain.user.service.AuthService;
import com.jlptcloud.domain.word.entity.UserWordStatus;
import com.jlptcloud.domain.word.entity.Word;
import com.jlptcloud.domain.word.repository.UserWordStatusRepository;
import com.jlptcloud.domain.word.repository.WordRepository;
import com.jlptcloud.global.exception.BusinessException;
import com.jlptcloud.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<WordResponse> getWords(
            JlptLevel jlptLevel,
            StudyStatus studyStatus,
            String keyword,
            Long userId,
            Pageable pageable
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (userId != null && studyStatus != null) {
            return getWordsByUserStatus(jlptLevel, studyStatus, normalizedKeyword, userId, pageable);
        }

        Page<Word> words = wordRepository.search(jlptLevel, studyStatus, normalizedKeyword, pageable);
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
        return WordResponse.from(word, userWordStatus);
    }

    @Transactional
    public WordResponse reviewWord(Long id, ReviewAnswerRequest request, Long userId) {
        AppUser user = authService.getUser(userId);
        Word word = findWord(id);
        UserWordStatus userWordStatus = userWordStatusRepository.findByUser_IdAndWord_Id(user.getId(), word.getId())
                .orElseGet(() -> new UserWordStatus(user, word, StudyStatus.NEW));

        userWordStatus.recordReview(Boolean.TRUE.equals(request.correct()), LocalDateTime.now());
        userWordStatusRepository.save(userWordStatus);
        return WordResponse.from(word, userWordStatus);
    }

    public StudyProgressResponse getProgress(Long userId) {
        AppUser user = authService.getUser(userId);
        long totalWords = wordRepository.count();
        long dueReviews = userWordStatusRepository.countByUser_IdAndNextReviewAtLessThanEqual(user.getId(), LocalDateTime.now());

        List<LevelProgressResponse> levels = java.util.Arrays.stream(JlptLevel.values())
                .map(level -> buildLevelProgress(user.getId(), level))
                .toList();

        long mastered = levels.stream().mapToLong(LevelProgressResponse::masteredCount).sum();
        long reviewNeeded = levels.stream().mapToLong(LevelProgressResponse::reviewNeededCount).sum();
        return new StudyProgressResponse(totalWords, dueReviews, reviewNeeded, completionRate(mastered, totalWords), levels);
    }

    private Page<WordResponse> getWordsByUserStatus(
            JlptLevel jlptLevel,
            StudyStatus studyStatus,
            String keyword,
            Long userId,
            Pageable pageable
    ) {
        List<Word> scopedWords = loadScopedWords(jlptLevel, keyword, pageable.getSort());
        Map<Long, UserWordStatus> statuses = userWordStatusRepository.findByUser_IdAndWordIn(userId, scopedWords)
                .stream()
                .collect(Collectors.toMap(status -> status.getWord().getId(), Function.identity()));

        List<WordResponse> filteredResponses = scopedWords.stream()
                .map(word -> WordResponse.from(word, statuses.get(word.getId())))
                .filter(response -> response.studyStatus() == studyStatus)
                .toList();

        return paginateResponses(filteredResponses, pageable);
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
                .map(word -> WordResponse.from(word, statuses.get(word.getId())))
                .toList();
        return new PageImpl<>(responses, words.getPageable(), words.getTotalElements());
    }

    private List<Word> loadScopedWords(JlptLevel jlptLevel, String keyword, Sort sort) {
        Sort effectiveSort = sort.isSorted() ? sort : Sort.by(Sort.Direction.ASC, "id");
        return wordRepository.searchScoped(jlptLevel, keyword, effectiveSort);
    }

    private Page<WordResponse> paginateResponses(List<WordResponse> responses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= responses.size()) {
            return new PageImpl<>(List.of(), pageable, responses.size());
        }

        int end = Math.min(start + pageable.getPageSize(), responses.size());
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    private Word findWord(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORD_NOT_FOUND));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private LevelProgressResponse buildLevelProgress(Long userId, JlptLevel level) {
        long total = wordRepository.countByJlptLevel(level);
        long recorded = userWordStatusRepository.countByUser_IdAndWord_JlptLevel(userId, level);
        long explicitNew = userWordStatusRepository.countByUser_IdAndWord_JlptLevelAndStudyStatus(userId, level, StudyStatus.NEW);
        long learning = userWordStatusRepository.countByUser_IdAndWord_JlptLevelAndStudyStatus(userId, level, StudyStatus.LEARNING);
        long reviewNeeded = userWordStatusRepository.countByUser_IdAndWord_JlptLevelAndStudyStatus(userId, level, StudyStatus.REVIEW_NEEDED);
        long mastered = userWordStatusRepository.countByUser_IdAndWord_JlptLevelAndStudyStatus(userId, level, StudyStatus.MASTERED);
        long newCount = Math.max(0, total - recorded + explicitNew);
        return new LevelProgressResponse(level, total, newCount, learning, reviewNeeded, mastered, completionRate(mastered, total));
    }

    private int completionRate(long mastered, long total) {
        if (total == 0) {
            return 0;
        }
        return (int) Math.round((mastered * 100.0) / total);
    }
}
