package com.twitter.api.service;

import com.twitter.api.dto.LikeRequest;
import com.twitter.api.dto.MessageResponse;
import com.twitter.api.entity.Like;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import com.twitter.api.exception.ResourceNotFoundException;
import com.twitter.api.repository.LikeRepository;
import com.twitter.api.repository.TweetRepository;
import com.twitter.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private Tweet tweet;
    private Like like;
    private LikeRequest likeRequest;

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
                .build();

        like = Like.builder()
                .id(1L)
                .user(user)
                .tweet(tweet)
                .build();

        likeRequest = new LikeRequest();
        likeRequest.setTweetId(1L);
    }

    @Test
    public void testLikeTweet() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(likeRepository.findByUserAndTweet(user, tweet)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(like);

        MessageResponse response = likeService.likeTweet(likeRequest, "testuser");

        assertNotNull(response);
        assertTrue(response.getMessage().contains("successfully"));
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    public void testLikeTweetAlreadyLiked() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(likeRepository.findByUserAndTweet(user, tweet)).thenReturn(Optional.of(like));

        MessageResponse response = likeService.likeTweet(likeRequest, "testuser");

        assertNotNull(response);
        assertTrue(response.getMessage().contains("already liked"));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    public void testDislikeTweet() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(likeRepository.findByUserAndTweet(user, tweet)).thenReturn(Optional.of(like));
        doNothing().when(likeRepository).delete(like);

        MessageResponse response = likeService.dislikeTweet(likeRequest, "testuser");

        assertNotNull(response);
        assertTrue(response.getMessage().contains("successfully"));
        verify(likeRepository, times(1)).delete(like);
    }

    @Test
    public void testDislikeTweetNotLiked() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(likeRepository.findByUserAndTweet(user, tweet)).thenReturn(Optional.empty());

        MessageResponse response = likeService.dislikeTweet(likeRequest, "testuser");

        assertNotNull(response);
        assertTrue(response.getMessage().contains("not liked"));
        verify(likeRepository, never()).delete(any(Like.class));
    }
} 