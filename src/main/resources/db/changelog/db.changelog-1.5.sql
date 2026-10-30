--liquibase formatted sql
--changeset chatter:6

ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
