--liquibase formatted sql
--changeset chatter:4

ALTER TABLE posts
    ALTER COLUMN content TYPE VARCHAR(280);
