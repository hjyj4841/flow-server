package com.master.flow.controller;

import com.master.flow.model.vo.Comment;
import com.master.flow.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge=6000)
public class CommentController {

    /* 댓글 작성 */
    @Autowired
    private CommentService commentService;

    @PostMapping("/mm")
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        Comment savedComment = commentService.saveComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);

//    댓글 한개 삭제(대댓글 미고려)
    @DeleteMapping("/delComment")
    public ResponseEntity delete (int commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
