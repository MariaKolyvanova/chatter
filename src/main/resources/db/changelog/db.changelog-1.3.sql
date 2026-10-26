--liquibase formatted sql
--changeset microblogging:4

ALTER TABLE posts
    ALTER COLUMN content TYPE VARCHAR(280);
