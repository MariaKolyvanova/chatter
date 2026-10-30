package com.kolyvanova.chatter.topicSubscriptions;

import com.kolyvanova.chatter.post.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicSubscriptionsRepository extends JpaRepository<TopicSubscriptions, UUID> {

    boolean existsByUserIdAndCategory(UUID userId, PostCategory category);

    Optional<TopicSubscriptions> findByUserIdAndCategory(UUID userId, PostCategory category);

    List<TopicSubscriptions> findAllByUserId(UUID userId);
}
