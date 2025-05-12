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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TweetRepository tweetRepository;
    
    @Transactional
    public MessageResponse likeTweet(LikeRequest likeRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Tweet tweet = tweetRepository.findById(likeRequest.getTweetId())
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + likeRequest.getTweetId()));
        
        // Check if the user already liked the tweet
        if (likeRepository.existsByUserAndTweet(user, tweet)) {
            return new MessageResponse("You have already liked this tweet");
        }
        
        Like like = Like.builder()
                .user(user)
                .tweet(tweet)
                .build();
        
        likeRepository.save(like);
        
        return new MessageResponse("Tweet liked successfully");
    }
    
    @Transactional
    public MessageResponse dislikeTweet(LikeRequest likeRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Tweet tweet = tweetRepository.findById(likeRequest.getTweetId())
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + likeRequest.getTweetId()));
        
        // Check if the user liked the tweet
        if (!likeRepository.existsByUserAndTweet(user, tweet)) {
            return new MessageResponse("You have not liked this tweet");
        }
        
        likeRepository.deleteByUserAndTweet(user, tweet);
        
        return new MessageResponse("Tweet disliked successfully");
    }
} 