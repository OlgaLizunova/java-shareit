package ru.practicum.shareit.item.comment.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.comment.dto.CommentInputDto;
import ru.practicum.shareit.item.comment.dto.CommentOutputDto;
import ru.practicum.shareit.item.comment.dto.SavedCommentOutputDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface CommentMapper {
    Comment toComment(CommentInputDto commentInputDto);

    @Mapping(target = "authorName", source = "author.name")
    SavedCommentOutputDto toSavedCommentOutputDto(Comment comment);

    @Mapping(target = "authorName", source = "author.name")
    CommentOutputDto toCommentOutputDto(Comment comment);


    List<CommentOutputDto> outputMap(List<Comment> comments);
}
