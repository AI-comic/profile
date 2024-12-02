package kopo.aisw.springboot_basic.controller;

import kopo.aisw.springboot_basic.domain.Comment;
import kopo.aisw.springboot_basic.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Comment comment,
                                       Authentication auth) {
        comment.setUserId(auth.getName());
        commentService.saveComment(comment);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentsByBoardId(boardId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> update(@PathVariable Long commentId,
                                       @RequestBody Comment comment,
                                       Authentication auth) {
        Comment existingComment = commentService.getCommentById(commentId);
        if (!existingComment.getUserId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        comment.setCommentId(commentId);
        commentService.updateComment(comment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId,
                                       Authentication auth) {
        Comment comment = commentService.getCommentById(commentId);
        if (!comment.getUserId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
