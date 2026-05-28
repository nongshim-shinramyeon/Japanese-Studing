ALTER TABLE community_post
    ADD COLUMN user_id BIGINT;

ALTER TABLE community_comment
    ADD COLUMN user_id BIGINT;

INSERT INTO app_user (username, password_hash, created_at, updated_at)
SELECT 'legacy_user',
       '$2a$10$dXJ3SW6G7P50lGmMkkLx3uM9QlrUdZ9S4G8czhGAP0fsdImXvI1qS',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE EXISTS (
    SELECT 1
    FROM community_post post
    WHERE post.owner_key !~ '^user:[0-9]+$'
       OR (
            post.owner_key ~ '^user:[0-9]+$'
            AND NOT EXISTS (
                SELECT 1
                FROM app_user app_user
                WHERE app_user.id = substring(post.owner_key from 6)::BIGINT
            )
        )
)
OR EXISTS (
    SELECT 1
    FROM community_comment comment
    WHERE comment.owner_key !~ '^user:[0-9]+$'
       OR (
            comment.owner_key ~ '^user:[0-9]+$'
            AND NOT EXISTS (
                SELECT 1
                FROM app_user app_user
                WHERE app_user.id = substring(comment.owner_key from 6)::BIGINT
            )
        )
)
ON CONFLICT (username) DO NOTHING;

UPDATE community_post post
SET user_id = COALESCE(
        CASE
            WHEN post.owner_key ~ '^user:[0-9]+$'
                 AND EXISTS (
                    SELECT 1
                    FROM app_user app_user
                    WHERE app_user.id = substring(post.owner_key from 6)::BIGINT
                 )
                THEN substring(post.owner_key from 6)::BIGINT
            ELSE NULL
        END,
        (SELECT app_user.id FROM app_user app_user WHERE app_user.username = 'legacy_user')
    );

UPDATE community_comment comment
SET user_id = COALESCE(
        CASE
            WHEN comment.owner_key ~ '^user:[0-9]+$'
                 AND EXISTS (
                    SELECT 1
                    FROM app_user app_user
                    WHERE app_user.id = substring(comment.owner_key from 6)::BIGINT
                 )
                THEN substring(comment.owner_key from 6)::BIGINT
            ELSE NULL
        END,
        (SELECT app_user.id FROM app_user app_user WHERE app_user.username = 'legacy_user')
    );

ALTER TABLE community_post
    ALTER COLUMN user_id SET NOT NULL,
    ADD CONSTRAINT fk_community_post_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

ALTER TABLE community_comment
    ALTER COLUMN user_id SET NOT NULL,
    ADD CONSTRAINT fk_community_comment_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

CREATE INDEX idx_community_post_user ON community_post (user_id);
CREATE INDEX idx_community_comment_user ON community_comment (user_id);

ALTER TABLE community_post DROP COLUMN owner_key;
ALTER TABLE community_comment DROP COLUMN owner_key;
