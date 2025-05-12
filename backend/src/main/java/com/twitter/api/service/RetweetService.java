package com.twitter.api.service;

import com.twitter.api.dto.MessageResponse;
import com.twitter.api.dto.RetweetRequest;
import com.twitter.api.dto.RetweetResponse;
import com.twitter.api.dto.TweetResponse;
import com.twitter.api.dto.UserSummary;
import com.twitter.api.entity.Retweet;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import com.twitter.api.exception.ResourceNotFoundException;
import com.twitter.api.repository.RetweetRepository;
import com.twitter.api.repository.TweetRepository;
import com.twitter.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RetweetService {
    
    @Autowired
    private RetweetRepository retweetRepository;
    
    @Autowired
    private TweetRepository tweetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TweetService tweetService;
    
    @Transactional
    public RetweetResponse retweetTweet(RetweetRequest retweetRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Tweet tweet = tweetRepository.findById(retweetRequest.getTweetId())
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + retweetRequest.getTweetId()));
        
        // Check if the user already retweeted the tweet
        if (retweetRepository.existsByUserAndTweet(user, tweet)) {
            throw new IllegalStateException("You have already retweeted this tweet");
        }
        
        Retweet retweet = Retweet.builder()
                .user(user)
                .tweet(tweet)
                .comment(retweetRequest.getComment())
                .build();
        
        Retweet savedRetweet = retweetRepository.save(retweet);
        
        return convertToRetweetResponse(savedRetweet, username);
    }
    
    public List<RetweetResponse> getRetweetsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return retweetRepository.findByUser(user).stream()
                .map(retweet -> convertToRetweetResponse(retweet, username))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteRetweet(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Retweet retweet = retweetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retweet not found with id: " + id));
        
        if (!retweet.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this retweet");
        }
        
        retweetRepository.delete(retweet);
    }
    
    private RetweetResponse convertToRetweetResponse(Retweet retweet, String currentUsername) {
        UserSummary userSummary = UserSummary.builder()
                .id(retweet.getUser().getId())
                .username(retweet.getUser().getUsername())
                .email(retweet.getUser().getEmail())
                .bio(retweet.getUser().getBio())
                .profileImageUrl(retweet.getUser().getProfileImageUrl())
                .build();
        
        TweetResponse originalTweetResponse = tweetService.getTweetById(retweet.getTweet().getId(), currentUsername);
        
        return RetweetResponse.builder()
                .id(retweet.getId())
                .comment(retweet.getComment())
                .user(userSummary)
                .originalTweet(originalTweetResponse)
                .createdAt(retweet.getCreatedAt())
                .updatedAt(retweet.getUpdatedAt())
                .build();
    }
} 