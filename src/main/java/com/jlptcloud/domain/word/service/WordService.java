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

        Page<Word> words = findWords(jlptLevel, studyStatus, normalizedKeyword, pageable);
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
    public WordResponse markStudied(Long id, Long userId) {
        AppUser user = authService.getUser(userId);
        Word word = findWord(id);
        LocalDateTime now = LocalDateTime.now();
        UserWordStatus userWordStatus = userWordStatusRepository.findByUser_IdAndWord_Id(user.getId(), word.getId())
                .orElseGet(() -> new UserWordStatus(user, word, StudyStatus.NEW));

        userWordStatus.markStudied(now);
        userWordStatusRepository.save(userWordStatus);
        return WordResponse.from(word, userWordStatus, now);
    }

    @Transactional
    public WordResponse reviewWord(Long id, ReviewAnswerRequest request, Long userId) {
        AppUser user = authService.getUser(userId);
        Word word = findWord(id);
        LocalDateTime now = LocalDateTime.now();
        UserWordStatus userWordStatus = userWordStatusRepository.findByUser_IdAndWord_Id(user.getId(), word.getId())
                .orElseGet(() -> new UserWordStatus(user, word, StudyStatus.NEW));

        userWordStatus.recordReview(Boolean.TRUE.equals(request.correct()), now);
        userWordStatusRepository.save(userWordStatus);
        return WordResponse.from(word, userWordStatus, now);
    }

    public Page<WordResponse> getReviewWords(
            JlptLevel jlptLevel,
            String keyword,
            Long userId,
            Pageable pageable
    ) {
        AppUser user = authService.getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        String normalizedKeyword = normalizeKeyword(keyword);

        return findReviewStatuses(user.getId(), jlptLevel, normalizedKeyword, now, pageable)
                .map(status -> WordResponse.from(status.getWord(), status, now));
    }

    private Page<UserWordStatus> findReviewStatuses(
            Long userId,
            JlptLevel jlptLevel,
            String keyword,
            LocalDateTime now,
            Pageable pageable
    ) {
        if (jlptLevel != null && keyword != null) {
            return userWordStatusRepository.findReviewPageByLevelAndKeyword(userId, jlptLevel, keyword, now, pageable);
        }
        if (jlptLevel != null) {
            return userWordStatusRepository.findReviewPageByLevel(userId, jlptLevel, now, pageable);
        }
        if (keyword != null) {
            return userWordStatusRepository.findReviewPageByKeyword(userId, keyword, now, pageable);
        }
        return userWordStatusRepository.findReviewPage(userId, now, pageable);
    }

    public StudyProgressResponse getProgress(Long userId) {
        AppUser user = authService.getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        long totalWords = wordRepository.count();
        List<UserWordStatus> studiedStatuses = userWordStatusRepository.findByUser_IdAndStudiedTrue(user.getId());
        long studiedWords = studiedStatuses.size();
        long dueReviews = studiedStatuses.stream()
                .filter(status -> status.isDueForReview(now))
                .count();
        long reviewNeeded = studiedStatuses.stream()
                .filter(status -> status.currentMemoryScore(now) < 70)
                .count();
        long stageSevenWords = studiedStatuses.stream()
                .filter(status -> status.getMemoryStage() == UserWordStatus.MAX_MEMORY_STAGE)
                .count();
        int averageMemoryScore = averageMemoryScore(studiedStatuses, now);

        List<LevelProgressResponse> levels = java.util.Arrays.stream(JlptLevel.values())
                .map(level -> buildLevelProgress(level, studiedStatuses, now))
                .toList();

        return new StudyProgressResponse(
                totalWords,
                studiedWords,
                dueReviews,
                reviewNeeded,
                completionRate(studiedWords, totalWords),
                averageMemoryScore,
                stageSevenWords,
                levels
        );
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
        LocalDateTime now = LocalDateTime.now();

        List<WordResponse> filteredResponses = scopedWords.stream()
                .map(word -> WordResponse.from(word, statuses.get(word.getId()), now))
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
        LocalDateTime now = LocalDateTime.now();

        List<WordResponse> responses = wordContent.stream()
                .map(word -> WordResponse.from(word, statuses.get(word.getId()), now))
                .toList();
        return new PageImpl<>(responses, words.getPageable(), words.getTotalElements());
    }

    private List<Word> loadScopedWords(JlptLevel jlptLevel, String keyword, Sort sort) {
        Sort effectiveSort = sort.isSorted() ? sort : Sort.by(Sort.Direction.ASC, "id");
        if (keyword == null) {
            if (jlptLevel != null) {
                return wordRepository.findAllByJlptLevel(jlptLevel, effectiveSort);
            }
            return wordRepository.findAll(effectiveSort);
        }
        return wordRepository.searchScoped(jlptLevel, keyword, effectiveSort);
    }

    private Page<Word> findWords(
            JlptLevel jlptLevel,
            StudyStatus studyStatus,
            String keyword,
            Pageable pageable
    ) {
        if (keyword != null) {
            return wordRepository.search(jlptLevel, studyStatus, keyword, pageable);
        }
        if (jlptLevel != null && studyStatus != null) {
            return wordRepository.findByJlptLevelAndStudyStatus(jlptLevel, studyStatus, pageable);
        }
        if (jlptLevel != null) {
            return wordRepository.findByJlptLevel(jlptLevel, pageable);
        }
        if (studyStatus != null) {
            return wordRepository.findByStudyStatus(studyStatus, pageable);
        }
        return wordRepository.findAll(pageable);
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

    private LevelProgressResponse buildLevelProgress(
            JlptLevel level,
            List<UserWordStatus> studiedStatuses,
            LocalDateTime now
    ) {
        long total = wordRepository.countByJlptLevel(level);
        List<UserWordStatus> levelStatuses = studiedStatuses.stream()
                .filter(status -> status.getWord().getJlptLevel() == level)
                .toList();
        long studiedCount = levelStatuses.size();
        long learning = levelStatuses.stream()
                .filter(status -> status.getMemoryStage() < UserWordStatus.MAX_MEMORY_STAGE)
                .count();
        long reviewNeeded = levelStatuses.stream()
                .filter(status -> status.currentMemoryScore(now) < 70)
                .count();
        long mastered = levelStatuses.stream()
                .filter(status -> status.getMemoryStage() == UserWordStatus.MAX_MEMORY_STAGE)
                .count();
        long newCount = Math.max(0, total - studiedCount);
        return new LevelProgressResponse(
                level,
                total,
                newCount,
                studiedCount,
                learning,
                reviewNeeded,
                mastered,
                completionRate(studiedCount, total),
                averageMemoryScore(levelStatuses, now)
        );
    }

    private int completionRate(long mastered, long total) {
        if (total == 0) {
            return 0;
        }
        return (int) Math.round((mastered * 100.0) / total);
    }

    private int averageMemoryScore(List<UserWordStatus> statuses, LocalDateTime now) {
        if (statuses.isEmpty()) {
            return 0;
        }
        return (int) Math.round(statuses.stream()
                .mapToDouble(status -> status.currentMemoryScore(now))
                .average()
                .orElse(0.0));
    }

    private boolean matchesKeyword(Word word, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return word.getJapanese().toLowerCase().contains(lowerKeyword)
                || word.getReading().toLowerCase().contains(lowerKeyword)
                || word.getMeaning().toLowerCase().contains(lowerKeyword);
    }
}
