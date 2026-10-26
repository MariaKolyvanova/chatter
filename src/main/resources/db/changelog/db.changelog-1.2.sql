--liquibase formatted sql
--changeset microblogging:3

CREATE INDEX idx_posts_author_created_at ON posts (author_id, created_at);
CREATE INDEX idx_posts_reply_to_id ON posts (reply_to_id);
CREATE INDEX idx_posts_conversation_id ON posts (conversation_id);
CREATE INDEX idx_posts_published_roots_created_at
    ON posts (created_at)
    WHERE status = 'PUBLISHED' AND reply_to_id IS NULL;

CREATE INDEX idx_likes_user_id ON likes (user_id);
CREATE INDEX idx_reposts_user_id ON reposts (user_id);
CREATE INDEX idx_subscriptions_author_id ON subscriptions (author_id);
