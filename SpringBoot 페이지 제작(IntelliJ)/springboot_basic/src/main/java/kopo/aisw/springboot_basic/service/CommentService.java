package kopo.aisw.springboot_basic.service;

import kopo.aisw.springboot_basic.domain.Comment;
import kopo.aisw.springboot_basic.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    public void saveComment(Comment comment) {
        commentMapper.save(comment);
    }

    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentMapper.findByBoardId(boardId);
    }

    public void updateComment(Comment comment) {
        commentMapper.update(comment);
    }

    public void deleteComment(Long commentId) {
        commentMapper.delete(commentId);
    }

    public Comment getCommentById(Long commentId) {
        return commentMapper.findById(commentId);
    }
}
