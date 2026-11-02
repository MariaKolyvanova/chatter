--liquibase formatted sql
--changeset chatter:8

DROP TABLE IF EXISTS topic_subscriptions;
