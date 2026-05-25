CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE word (
    id BIGSERIAL PRIMARY KEY,
    japanese VARCHAR(100) NOT NULL,
    reading VARCHAR(100) NOT NULL,
    meaning VARCHAR(150) NOT NULL,
    part_of_speech VARCHAR(50) NOT NULL,
    example_sentence VARCHAR(500) NOT NULL,
    jlpt_level VARCHAR(10) NOT NULL,
    study_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE grammar_note (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    pattern_expression VARCHAR(120) NOT NULL,
    meaning VARCHAR(200) NOT NULL,
    explanation VARCHAR(1000) NOT NULL,
    example_sentence VARCHAR(500) NOT NULL,
    jlpt_level VARCHAR(10) NOT NULL,
    study_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE community_post (
    id BIGSERIAL PRIMARY KEY,
    author_name VARCHAR(80) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(3000) NOT NULL,
    owner_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE community_comment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES community_post(id),
    parent_id BIGINT REFERENCES community_comment(id),
    author_name VARCHAR(80) NOT NULL,
    content VARCHAR(1200) NOT NULL,
    owner_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_word_status (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    word_id BIGINT NOT NULL REFERENCES word(id),
    study_status VARCHAR(20) NOT NULL,
    correct_streak INTEGER NOT NULL DEFAULT 0,
    wrong_count INTEGER NOT NULL DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    next_review_at TIMESTAMP,
    last_reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_word_status_user_word UNIQUE (user_id, word_id)
);

CREATE INDEX idx_word_jlpt_status ON word (jlpt_level, study_status);
CREATE INDEX idx_word_search_japanese ON word USING gin (lower(japanese) gin_trgm_ops);
CREATE INDEX idx_word_search_reading ON word USING gin (lower(reading) gin_trgm_ops);
CREATE INDEX idx_word_search_meaning ON word USING gin (lower(meaning) gin_trgm_ops);
CREATE INDEX idx_grammar_jlpt_status ON grammar_note (jlpt_level, study_status);
CREATE INDEX idx_community_post_created_at ON community_post (created_at DESC);
CREATE INDEX idx_community_comment_post_parent ON community_comment (post_id, parent_id);
CREATE INDEX idx_user_word_status_progress ON user_word_status (user_id, study_status, next_review_at);
