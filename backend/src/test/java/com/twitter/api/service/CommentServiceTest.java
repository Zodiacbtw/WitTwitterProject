package com.twitter.api.service;

import com.twitter.api.dto.CommentRequest;
import com.twitter.api.dto.CommentResponse;
import com.twitter.api.entity.Comment;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import com.twitter.api.exception.ResourceNotFoundException;
import com.twitter.api.exception.UnauthorizedException;
import com.twitter.api.repository.CommentRepository;
import com.twitter.api.repository.TweetRepository;
import com.twitter.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Tweet tweet;
    private Comment comment;
    private CommentRequest commentRequest;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        tweet = Tweet.builder()
                .id(1L)
                .content("Test tweet content")
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        comment = Comment.builder()
                .id(1L)
                .content("Test comment content")
                .user(user)
                .tweet(tweet)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        commentRequest = new CommentRequest();
        commentRequest.setContent("Test comment content");
        commentRequest.setTweetId(1L);
    }

    @Test
    public void testAddComment() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.addComment(commentRequest, "testuser");

        assertNotNull(response);
        assertEquals("Test comment content", response.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void testGetCommentsByTweetId() {
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(commentRepository.findByTweetOrderByCreatedAtDesc(tweet)).thenReturn(Arrays.asList(comment));

        List<CommentResponse> responses = commentService.getCommentsByTweetId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test comment content", responses.get(0).getContent());
        verify(commentRepository, times(1)).findByTweetOrderByCreatedAtDesc(tweet);
    }

    @Test
    public void testUpdateComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentRequest.setContent("Updated comment content");
        
        CommentResponse response = commentService.updateComment(1L, commentRequest, "testuser");
        
        assertNotNull(response);
        assertEquals("Updated comment content", response.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void testUpdateCommentUnauthorized() {
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .build();
                
        Comment commentByAnotherUser = Comment.builder()
                .id(1L)
                .content("Test comment content")
                .user(anotherUser) // Different user
                .tweet(tweet)
                .build();
                
        when(commentRepository.findById(1L)).thenReturn(Optional.of(commentByAnotherUser));

        assertThrows(UnauthorizedException.class, () -> {
            commentService.updateComment(1L, commentRequest, "testuser");
        });
    }

    @Test
    public void testDeleteComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);
        
        commentService.deleteComment(1L, "testuser");
        
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    public void testDeleteCommentWithTweetOwnerPermission() {
        User commentAuthor = User.builder()
                .id(2L)
                .username("commentAuthor")
                .build();
                
        Comment commentByAnotherUser = Comment.builder()
                .id(1L)
                .content("Test comment content")
                .user(commentAuthor)
                .tweet(tweet) // Tweet is owned by 'testuser'
                .build();
                
        when(commentRepository.findById(1L)).thenReturn(Optional.of(commentByAnotherUser));
        doNothing().when(commentRepository).delete(commentByAnotherUser);
        
        // testuser is the tweet owner, should be able to delete
        commentService.deleteComment(1L, "testuser");
        
        verify(commentRepository, times(1)).delete(commentByAnotherUser);
    }
} 