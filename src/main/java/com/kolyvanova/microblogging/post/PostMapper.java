package com.kolyvanova.microblogging.post;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(PostCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PostUpdateDto dto, @MappingTarget Post post);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "replyToId", source = "replyTo.id")
    @Mapping(target = "conversationId", source = "conversation.id")
    PostReadDto toReadDto(Post post);
}
