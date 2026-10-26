package com.kolyvanova.microblogging.like;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "userId", source = "user.id")
    LikeReadDto toReadDto(Like like);
}
