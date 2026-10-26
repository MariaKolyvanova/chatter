--liquibase formatted sql
--changeset microblogging:5

CREATE TABLE topic_subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    category VARCHAR(30) NOT NULL,
    CONSTRAINT fk_topic_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_topic_subscriptions_user_category UNIQUE (user_id, category)
);

CREATE INDEX idx_posts_published_roots_category_created_at
    ON posts (category, created_at)
    WHERE status = 'PUBLISHED' AND reply_to_id IS NULL;
