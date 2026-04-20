package com.mysite.sbb.domain.grammar.entity;

import com.mysite.sbb.domain.study.JlptLevel;
import com.mysite.sbb.domain.study.StudyStatus;
import com.mysite.sbb.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class GrammarNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 120)
    private String patternExpression;

    @Column(nullable = false, length = 200)
    private String meaning;

    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(nullable = false, length = 500)
    private String exampleSentence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JlptLevel jlptLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus studyStatus;

    protected GrammarNote() {
    }

    public GrammarNote(String title, String patternExpression, String meaning, String explanation,
                       String exampleSentence, JlptLevel jlptLevel, StudyStatus studyStatus) {
        this.title = title;
        this.patternExpression = patternExpression;
        this.meaning = meaning;
        this.explanation = explanation;
        this.exampleSentence = exampleSentence;
        this.jlptLevel = jlptLevel;
        this.studyStatus = studyStatus;
    }

    public void update(String title, String patternExpression, String meaning, String explanation,
                       String exampleSentence, JlptLevel jlptLevel, StudyStatus studyStatus) {
        this.title = title;
        this.patternExpression = patternExpression;
        this.meaning = meaning;
        this.explanation = explanation;
        this.exampleSentence = exampleSentence;
        this.jlptLevel = jlptLevel;
        this.studyStatus = studyStatus;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPatternExpression() {
        return patternExpression;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public JlptLevel getJlptLevel() {
        return jlptLevel;
    }

    public StudyStatus getStudyStatus() {
        return studyStatus;
    }
}
