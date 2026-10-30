package com.kolyvanova.chatter.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "subscriberId", source = "subscriber.id")
    @Mapping(target = "authorId", source = "author.id")
    SubscriptionReadDto toReadDto(Subscription subscription);
}
