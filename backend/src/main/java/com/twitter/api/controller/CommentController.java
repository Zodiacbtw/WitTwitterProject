package com.twitter.api.controller;

import com.twitter.api.dto.CommentRequest;
import com.twitter.api.dto.CommentResponse;
import com.twitter.api.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CommentRequest commentRequest,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        CommentResponse newComment = commentService.addComment(commentRequest, userDetails.getUsername());
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @GetMapping("/tweet/{tweetId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByTweetId(@PathVariable Long tweetId) {
        List<CommentResponse> comments = commentService.getCommentsByTweetId(tweetId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id,
                                                        @Valid @RequestBody CommentRequest commentRequest,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        CommentResponse updatedComment = commentService.updateComment(id, commentRequest, userDetails.getUsername());
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
} 