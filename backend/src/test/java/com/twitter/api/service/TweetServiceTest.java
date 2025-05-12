package com.twitter.api.service;

import com.twitter.api.dto.TweetRequest;
import com.twitter.api.dto.TweetResponse;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import com.twitter.api.exception.ResourceNotFoundException;
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
public class TweetServiceTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TweetService tweetService;

    private User user;
    private Tweet tweet;
    private TweetRequest tweetRequest;

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

        tweetRequest = new TweetRequest();
        tweetRequest.setContent("Test tweet content");
    }

    @Test
    public void testCreateTweet() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tweetRepository.save(any(Tweet.class))).thenReturn(tweet);

        TweetResponse response = tweetService.createTweet(tweetRequest, "testuser");

        assertNotNull(response);
        assertEquals("Test tweet content", response.getContent());
        assertEquals("testuser", response.getUsername());
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    public void testGetTweetsByUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tweetRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Arrays.asList(tweet));

        List<TweetResponse> responses = tweetService.getTweetsByUserId(1L, "testuser");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test tweet content", responses.get(0).getContent());
        verify(tweetRepository, times(1)).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    public void testGetTweetById() {
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));

        TweetResponse response = tweetService.getTweetById(1L, "testuser");

        assertNotNull(response);
        assertEquals("Test tweet content", response.getContent());
        verify(tweetRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetTweetByIdNotFound() {
        when(tweetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.getTweetById(1L, "testuser");
        });
    }

    @Test
    public void testUpdateTweet() {
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(tweetRepository.save(any(Tweet.class))).thenReturn(tweet);

        tweetRequest.setContent("Updated content");
        
        TweetResponse response = tweetService.updateTweet(1L, tweetRequest, "testuser");
        
        assertNotNull(response);
        assertEquals("Updated content", response.getContent());
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    public void testDeleteTweet() {
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        doNothing().when(tweetRepository).delete(tweet);
        
        tweetService.deleteTweet(1L, "testuser");
        
        verify(tweetRepository, times(1)).delete(tweet);
    }
} 