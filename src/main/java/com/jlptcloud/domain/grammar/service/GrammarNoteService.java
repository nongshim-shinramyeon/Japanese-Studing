package com.jlptcloud.domain.grammar.service;

import com.jlptcloud.domain.grammar.dto.request.GrammarNoteRequest;
import com.jlptcloud.domain.grammar.dto.response.GrammarNoteResponse;
import com.jlptcloud.domain.grammar.entity.GrammarNote;
import com.jlptcloud.domain.grammar.repository.GrammarNoteRepository;
import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.global.exception.BusinessException;
import com.jlptcloud.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GrammarNoteService {

    private final GrammarNoteRepository grammarNoteRepository;

    public GrammarNoteService(GrammarNoteRepository grammarNoteRepository) {
        this.grammarNoteRepository = grammarNoteRepository;
    }

    @Transactional
    public GrammarNoteResponse create(GrammarNoteRequest request) {
        GrammarNote grammarNote = new GrammarNote(
                request.title(),
                request.patternExpression(),
                request.meaning(),
                request.explanation(),
                request.exampleSentence(),
                request.jlptLevel(),
                request.studyStatus()
        );
        return GrammarNoteResponse.from(grammarNoteRepository.save(grammarNote));
    }

    public Page<GrammarNoteResponse> getGrammarNotes(JlptLevel jlptLevel, StudyStatus studyStatus, Pageable pageable) {
        Page<GrammarNote> grammarNotes;
        if (jlptLevel != null && studyStatus != null) {
            grammarNotes = grammarNoteRepository.findByJlptLevelAndStudyStatus(jlptLevel, studyStatus, pageable);
        } else if (jlptLevel != null) {
            grammarNotes = grammarNoteRepository.findByJlptLevel(jlptLevel, pageable);
        } else if (studyStatus != null) {
            grammarNotes = grammarNoteRepository.findByStudyStatus(studyStatus, pageable);
        } else {
            grammarNotes = grammarNoteRepository.findAll(pageable);
        }
        return grammarNotes.map(GrammarNoteResponse::from);
    }

    public GrammarNoteResponse getGrammarNote(Long id) {
        return GrammarNoteResponse.from(findGrammarNote(id));
    }

    @Transactional
    public GrammarNoteResponse update(Long id, GrammarNoteRequest request) {
        GrammarNote grammarNote = findGrammarNote(id);
        grammarNote.update(
                request.title(),
                request.patternExpression(),
                request.meaning(),
                request.explanation(),
                request.exampleSentence(),
                request.jlptLevel(),
                request.studyStatus()
        );
        return GrammarNoteResponse.from(grammarNote);
    }

    @Transactional
    public void delete(Long id) {
        grammarNoteRepository.delete(findGrammarNote(id));
    }

    private GrammarNote findGrammarNote(Long id) {
        return grammarNoteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRAMMAR_NOTE_NOT_FOUND));
    }
}
