package com.kolyvanova.microblogging.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    boolean existsBySubscriberIdAndAuthorId(UUID subscriberId, UUID authorId);

    Page<Subscription> findAllBySubscriberId(UUID subscriberId, Pageable pageable);

    Page<Subscription> findAllByAuthorId(UUID authorId, Pageable pageable);

    Optional<Subscription> findBySubscriberIdAndAuthorId(UUID subscriberId, UUID authorId);
}
