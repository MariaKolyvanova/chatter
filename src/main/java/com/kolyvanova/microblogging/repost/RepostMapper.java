package com.kolyvanova.microblogging.repost;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Repost toEntity(RepostCreateDto dto);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "userId", source = "user.id")
    RepostReadDto toReadDto(Repost repost);
}
