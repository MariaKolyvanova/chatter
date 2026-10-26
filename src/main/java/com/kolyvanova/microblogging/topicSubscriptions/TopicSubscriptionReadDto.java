package com.kolyvanova.microblogging.topicSubscriptions;

import com.kolyvanova.microblogging.post.PostCategory;

import java.util.UUID;

public record TopicSubscriptionReadDto(
        UUID id,
        UUID userId,
        PostCategory category
) {
}
