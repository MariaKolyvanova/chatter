package com.kolyvanova.microblogging.subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriber", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Subscription toEntity(SubscriptionCreateDto dto);

    @Mapping(target = "subscriberId", source = "subscriber.id")
    @Mapping(target = "authorId", source = "author.id")
    SubscriptionReadDto toReadDto(Subscription subscription);
}
