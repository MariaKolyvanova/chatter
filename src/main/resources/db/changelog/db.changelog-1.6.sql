--liquibase formatted sql
--changeset chatter:7

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_key VARCHAR(255);

ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS image_key VARCHAR(255);
