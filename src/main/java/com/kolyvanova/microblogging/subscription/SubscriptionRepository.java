package com.kolyvanova.microblogging.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    boolean existsBySubscriberIdAndAuthorId(UUID subscriberId, UUID authorId);

    List<Subscription> findAllBySubscriberId(UUID subscriberId);

    List<Subscription> findAllByAuthorId(UUID authorId);
}
