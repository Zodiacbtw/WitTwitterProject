package com.twitter.api.service;

import com.twitter.api.dto.CommentRequest;
import com.twitter.api.dto.CommentResponse;
import com.twitter.api.dto.UserSummary;
import com.twitter.api.entity.Comment;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import com.twitter.api.exception.ResourceNotFoundException;
import com.twitter.api.repository.CommentRepository;
import com.twitter.api.repository.TweetRepository;
import com.twitter.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private TweetRepository tweetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public CommentResponse addComment(CommentRequest commentRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Tweet tweet = tweetRepository.findById(commentRequest.getTweetId())
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + commentRequest.getTweetId()));
        
        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .user(user)
                .tweet(tweet)
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        return convertToCommentResponse(savedComment);
    }
    
    public List<CommentResponse> getCommentsByTweetId(Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + tweetId));
        
        return commentRepository.findByTweetOrderByCreatedAtDesc(tweet).stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest commentRequest, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this comment");
        }
        
        comment.setContent(commentRequest.getContent());
        
        Comment updatedComment = commentRepository.save(comment);
        
        return convertToCommentResponse(updatedComment);
    }
    
    @Transactional
    public void deleteComment(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        
        // Check if the user is the comment owner or the tweet owner
        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isTweetOwner = comment.getTweet().getUser().getId().equals(user.getId());
        
        if (!isCommentOwner && !isTweetOwner) {
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }
    
    private CommentResponse convertToCommentResponse(Comment comment) {
        UserSummary userSummary = UserSummary.builder()
                .id(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .email(comment.getUser().getEmail())
                .bio(comment.getUser().getBio())
                .profileImageUrl(comment.getUser().getProfileImageUrl())
                .build();
        
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userSummary)
                .tweetId(comment.getTweet().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
} 