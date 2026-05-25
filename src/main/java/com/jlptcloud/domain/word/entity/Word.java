package com.jlptcloud.domain.word.entity;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Word extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String japanese;

    @Column(nullable = false, length = 100)
    private String reading;

    @Column(nullable = false, length = 150)
    private String meaning;

    @Column(nullable = false, length = 50)
    private String partOfSpeech;

    @Column(nullable = false, length = 500)
    private String exampleSentence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JlptLevel jlptLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus studyStatus;

    protected Word() {
    }

    public Word(String japanese, String reading, String meaning, String partOfSpeech, String exampleSentence,
                JlptLevel jlptLevel, StudyStatus studyStatus) {
        this.japanese = japanese;
        this.reading = reading;
        this.meaning = meaning;
        this.partOfSpeech = partOfSpeech;
        this.exampleSentence = exampleSentence;
        this.jlptLevel = jlptLevel;
        this.studyStatus = studyStatus;
    }

    public void update(String japanese, String reading, String meaning, String partOfSpeech, String exampleSentence,
                       JlptLevel jlptLevel, StudyStatus studyStatus) {
        this.japanese = japanese;
        this.reading = reading;
        this.meaning = meaning;
        this.partOfSpeech = partOfSpeech;
        this.exampleSentence = exampleSentence;
        this.jlptLevel = jlptLevel;
        this.studyStatus = studyStatus;
    }

    public Long getId() {
        return id;
    }

    public String getJapanese() {
        return japanese;
    }

    public String getReading() {
        return reading;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
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
