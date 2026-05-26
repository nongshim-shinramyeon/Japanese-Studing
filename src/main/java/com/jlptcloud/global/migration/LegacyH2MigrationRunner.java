package com.jlptcloud.global.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyH2MigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyH2MigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;
    private final String legacyUrl;

    public LegacyH2MigrationRunner(
            DataSource dataSource,
            @Value("${jlptcloud.legacy-h2-migration.enabled:false}") boolean enabled,
            @Value("${jlptcloud.legacy-h2-migration.url:jdbc:h2:file:/legacy-h2-data/jlptcloud;MODE=MYSQL;IFEXISTS=TRUE;ACCESS_MODE_DATA=r}") String legacyUrl
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.enabled = enabled;
        this.legacyUrl = legacyUrl;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (migrationAlreadyRan()) {
            resetSequences();
            log.info("Legacy H2 migration skipped because PostgreSQL already has app_user or word data. Sequences were synchronized.");
            return;
        }

        try (Connection legacy = DriverManager.getConnection(legacyUrl, "sa", "")) {
            migrateUsers(legacy);
            migrateWords(legacy);
            migrateGrammarNotes(legacy);
            migrateCommunityPosts(legacy);
            migrateCommunityComments(legacy);
            migrateUserWordStatuses(legacy);
            resetSequences();
            log.info("Legacy H2 migration completed.");
        } catch (SQLException ex) {
            log.warn("Legacy H2 migration skipped: {}", ex.getMessage());
        }
    }

    private boolean migrationAlreadyRan() {
        Long appUsers = jdbcTemplate.queryForObject("select count(*) from app_user", Long.class);
        Long words = jdbcTemplate.queryForObject("select count(*) from word", Long.class);
        return (appUsers != null && appUsers > 0) || (words != null && words > 0);
    }

    private void migrateUsers(Connection legacy) throws SQLException {
        if (!legacyTableExists(legacy, "APP_USER")) {
            return;
        }
        List<Object[]> rows = query(legacy, """
                select id, username, password_hash, created_at, updated_at
                from app_user
                order by id
                """, row -> new Object[] {
                        row.getLong("id"),
                        row.getString("username"),
                        row.getString("password_hash"),
                        timestamp(row, "created_at"),
                        timestamp(row, "updated_at")
                });
        jdbcTemplate.batchUpdate("""
                insert into app_user (id, username, password_hash, created_at, updated_at)
                values (?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, rows);
        log.info("Migrated {} users from legacy H2.", rows.size());
    }

    private void migrateWords(Connection legacy) throws SQLException {
        if (!legacyTableExists(legacy, "WORD")) {
            return;
        }
        List<Object[]> rows = query(legacy, """
                select id, japanese, reading, meaning, part_of_speech, example_sentence,
                       jlpt_level, study_status, created_at, updated_at
                from word
                order by id
                """, row -> new Object[] {
                        row.getLong("id"),
                        row.getString("japanese"),
                        row.getString("reading"),
                        row.getString("meaning"),
                        row.getString("part_of_speech"),
                        row.getString("example_sentence"),
                        row.getString("jlpt_level"),
                        row.getString("study_status"),
                        timestamp(row, "created_at"),
                        timestamp(row, "updated_at")
                });
        jdbcTemplate.batchUpdate("""
                insert into word (id, japanese, reading, meaning, part_of_speech, example_sentence,
                                  jlpt_level, study_status, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, rows);
        log.info("Migrated {} words from legacy H2.", rows.size());
    }

    private void migrateGrammarNotes(Connection legacy) throws SQLException {
        if (!legacyTableExists(legacy, "GRAMMAR_NOTE")) {
            return;
        }
        List<Object[]> rows = query(legacy, """
                select id, title, pattern_expression, meaning, explanation, example_sentence,
                       jlpt_level, study_status, created_at, updated_at
                from grammar_note
                order by id
                """, row -> new Object[] {
                        row.getLong("id"),
                        row.getString("title"),
                        row.getString("pattern_expression"),
                        row.getString("meaning"),
                        row.getString("explanation"),
                        row.getString("example_sentence"),
                        row.getString("jlpt_level"),
                        row.getString("study_status"),
                        timestamp(row, "created_at"),
                        timestamp(row, "updated_at")
                });
        jdbcTemplate.batchUpdate("""
                insert into grammar_note (id, title, pattern_expression, meaning, explanation, example_sentence,
                                          jlpt_level, study_status, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, rows);
        log.info("Migrated {} grammar notes from legacy H2.", rows.size());
    }

    private void migrateCommunityPosts(Connection legacy) throws SQLException {
        if (!legacyTableExists(legacy, "COMMUNITY_POST")) {
            return;
        }
        List<Object[]> rows = query(legacy, """
                select id, author_name, title, content, owner_key, created_at, updated_at
                from community_post
                order by id
                """, row -> new Object[] {
                        row.getLong("id"),
                        row.getString("author_name"),
                        row.getString("title"),
                        row.getString("content"),
                        defaultString(row.getString("owner_key"), "legacy-post:" + row.getLong("id")),
                        timestamp(row, "created_at"),
                        timestamp(row, "updated_at")
                });
        jdbcTemplate.batchUpdate("""
                insert into community_post (id, author_name, title, content, owner_key, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, rows);
        log.info("Migrated {} community posts from legacy H2.", rows.size());
    }

    private void migrateCommunityComments(Connection legacy) throws SQLException {
        if (!legacyTableExists(legacy, "COMMUNITY_COMMENT")) {
            return;
        }
        List<Object[]> rows = query(legacy, """
                select id, post_id, parent_id, author_name, content, created_at, updated_at
                from community_comment
                order by id
                """, row -> new Object[] {
                        row.getLong("id"),
                        row.getLong("post_id"),
                        nullableLong(row, "parent_id"),
                        row.getString("author_name"),
                        row.getString("content"),
                        "legacy-comment:" + row.getLong("id"),
                        timestamp(row, "created_at"),
                        timestamp(row, "updated_at")
                });
        jdbcTemplate.batchUpdate("""
                insert into community_comment (id, post_id, parent_id, author_name, content, owner_key, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, rows);
        log.info("Migrated {} community comments from legacy H2.", rows.size());
    }

    private void migrateUserWordStatuses(Connection legacy) throws SQLException {
        if (!legacyTableExists(legacy, "USER_WORD_STATUS")) {
            return;
        }
        List<Object[]> rows = query(legacy, """
                select id, user_id, word_id, study_status, correct_streak, wrong_count,
                       review_count, next_review_at, last_reviewed_at, created_at, updated_at
                from user_word_status
                order by id
                """, row -> {
                    String studyStatus = row.getString("study_status");
                    int wrongCount = row.getInt("wrong_count");
                    int reviewCount = row.getInt("review_count");
                    Timestamp createdAt = timestamp(row, "created_at");
                    Timestamp updatedAt = timestamp(row, "updated_at");
                    Timestamp lastReviewedAt = nullableTimestamp(row, "last_reviewed_at");
                    Timestamp nextReviewAt = nullableTimestamp(row, "next_review_at");
                    boolean studied = !"NEW".equals(studyStatus) || reviewCount > 0 || lastReviewedAt != null;
                    int memoryStage = memoryStageFromStatus(studyStatus);
                    double memoryScore = memoryScoreFromStatus(studyStatus);

                    if (studied && lastReviewedAt == null) {
                        lastReviewedAt = updatedAt != null ? updatedAt : createdAt;
                    }
                    if (studied && nextReviewAt == null && lastReviewedAt != null) {
                        nextReviewAt = Timestamp.valueOf(lastReviewedAt.toLocalDateTime().plusDays(1));
                    }

                    return new Object[] {
                            row.getLong("id"),
                            row.getLong("user_id"),
                            row.getLong("word_id"),
                            studyStatus,
                            studied,
                            memoryStage,
                            memoryScore,
                            row.getInt("correct_streak"),
                            Math.max(reviewCount - wrongCount, 0),
                            wrongCount,
                            reviewCount,
                            nextReviewAt,
                            lastReviewedAt,
                            createdAt,
                            updatedAt
                    };
                });
        jdbcTemplate.batchUpdate("""
                insert into user_word_status (id, user_id, word_id, study_status, studied, memory_stage,
                                              memory_score, correct_streak, correct_count, wrong_count,
                                              review_count, next_review_at, last_reviewed_at, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, rows);
        log.info("Migrated {} user word statuses from legacy H2.", rows.size());
    }

    private int memoryStageFromStatus(String studyStatus) {
        return switch (studyStatus) {
            case "MASTERED" -> 6;
            case "LEARNING" -> 2;
            default -> 1;
        };
    }

    private double memoryScoreFromStatus(String studyStatus) {
        return switch (studyStatus) {
            case "MASTERED" -> 90.0;
            case "LEARNING" -> 75.0;
            case "REVIEW_NEEDED" -> 45.0;
            default -> 0.0;
        };
    }

    private void resetSequences() {
        resetSequence("app_user", "app_user_id_seq");
        resetSequence("word", "word_id_seq");
        resetSequence("grammar_note", "grammar_note_id_seq");
        resetSequence("community_post", "community_post_id_seq");
        resetSequence("community_comment", "community_comment_id_seq");
        resetSequence("user_word_status", "user_word_status_id_seq");
    }

    private void resetSequence(String table, String sequence) {
        jdbcTemplate.queryForObject("""
                select setval(
                    ?,
                    greatest(coalesce((select max(id) from %s), 1), 1),
                    (select count(*) from %s) > 0
                )
                """.formatted(table, table), Long.class, sequence);
    }

    private boolean legacyTableExists(Connection legacy, String tableName) throws SQLException {
        try (ResultSet resultSet = legacy.getMetaData().getTables(null, null, tableName, null)) {
            if (resultSet.next()) {
                return true;
            }
        }
        try (ResultSet resultSet = legacy.getMetaData().getTables(null, null, tableName.toLowerCase(), null)) {
            return resultSet.next();
        }
    }

    private List<Object[]> query(Connection connection, String sql, RowMapper mapper) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                rows.add(mapper.map(resultSet));
            }
        }
        return rows;
    }

    private Timestamp timestamp(ResultSet row, String column) throws SQLException {
        Timestamp timestamp = row.getTimestamp(column);
        if (timestamp != null) {
            return timestamp;
        }
        return Timestamp.valueOf(LocalDateTime.now());
    }

    private Timestamp nullableTimestamp(ResultSet row, String column) throws SQLException {
        return row.getTimestamp(column);
    }

    private Long nullableLong(ResultSet row, String column) throws SQLException {
        long value = row.getLong(column);
        return row.wasNull() ? null : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @FunctionalInterface
    private interface RowMapper {
        Object[] map(ResultSet resultSet) throws SQLException;
    }
}
