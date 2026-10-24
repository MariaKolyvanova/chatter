--liquibase formatted sql
--changeset microblogging:2

DROP TABLE IF EXISTS comments;

ALTER TABLE posts
    ADD COLUMN reply_to_id UUID;

ALTER TABLE posts
    ADD COLUMN conversation_id UUID;

UPDATE posts
SET conversation_id = id
WHERE conversation_id IS NULL;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_reply_to
        FOREIGN KEY (reply_to_id) REFERENCES posts (id) ON DELETE CASCADE;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_conversation
        FOREIGN KEY (conversation_id) REFERENCES posts (id) ON DELETE CASCADE;