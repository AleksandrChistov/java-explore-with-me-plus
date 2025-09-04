package ru.practicum.explorewithme.event.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.comment.dao.CommentRepository;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.event.comment.model.Comment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCommentServiceImpl implements PublicCommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<ResponseCommentDto> getCommentsByEventId(Long eventId) {
        List<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, Status.PUBLISHED);
        return commentMapper.toCommentResponseDtos(comments);
    }

    @Override
    public List<ResponseCommentDto> getAllCommentsByEventIds(List<Long> eventIds) {
        List<Comment> comments = commentRepository.findByEventIdInAndStatus(eventIds, Status.PUBLISHED);
        return commentMapper.toCommentResponseDtos(comments);
    }


}
