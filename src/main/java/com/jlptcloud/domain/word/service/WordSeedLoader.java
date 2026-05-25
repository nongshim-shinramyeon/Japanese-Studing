package com.jlptcloud.domain.word.service;

import com.jlptcloud.domain.study.JlptLevel;
import com.jlptcloud.domain.study.StudyStatus;
import com.jlptcloud.domain.word.entity.Word;
import com.jlptcloud.domain.word.repository.WordRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WordSeedLoader implements CommandLineRunner {

    private static final String WORD_DATA_PATH = "data/jlpt-words.csv";

    private final WordRepository wordRepository;

    public WordSeedLoader(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        if (wordRepository.count() > 0) {
            return;
        }

        List<Word> words = loadWords();
        wordRepository.saveAll(words);
    }

    private List<Word> loadWords() throws IOException {
        ClassPathResource resource = new ClassPathResource(WORD_DATA_PATH);
        List<Word> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                List<String> columns = parseCsvLine(line);
                if (columns.size() < 6) {
                    continue;
                }

                words.add(new Word(
                        columns.get(1),
                        columns.get(2),
                        columns.get(3),
                        columns.get(4),
                        columns.get(5),
                        JlptLevel.valueOf(columns.get(0)),
                        StudyStatus.NEW
                ));
            }
        }

        return words;
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                columns.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        columns.add(value.toString());
        return columns;
    }
}
