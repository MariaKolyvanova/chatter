package com.kolyvanova.chatter.topicSubscriptions;

import com.kolyvanova.chatter.post.PostCategory;

import java.util.UUID;

public record TopicSubscriptionReadDto(
        UUID id,
        UUID userId,
        PostCategory category
) {
}
