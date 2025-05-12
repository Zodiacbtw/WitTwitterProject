package com.twitter.api.service;

import com.twitter.api.dto.TweetRequest;
import com.twitter.api.dto.TweetResponse;
import com.twitter.api.dto.UserSummary;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import com.twitter.api.exception.ResourceNotFoundException;
import com.twitter.api.repository.LikeRepository;
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
public class TweetService {
    
    @Autowired
    private TweetRepository tweetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private RetweetRepository retweetRepository;

    @Transactional
    public TweetResponse createTweet(TweetRequest tweetRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Tweet tweet = Tweet.builder()
                .content(tweetRequest.getContent())
                .imageUrl(tweetRequest.getImageUrl())
                .user(user)
                .build();
        
        Tweet savedTweet = tweetRepository.save(tweet);
        
        return convertToTweetResponse(savedTweet, user, false, false);
    }

    public List<TweetResponse> getTweetsByUserId(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        User currentUser = null;
        if (currentUsername != null && !currentUsername.isEmpty()) {
            currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        }
        
        final User finalCurrentUser = currentUser;
        
        return tweetRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(tweet -> {
                    boolean isLiked = false;
                    boolean isRetweeted = false;
                    
                    if (finalCurrentUser != null) {
                        isLiked = likeRepository.existsByUserAndTweet(finalCurrentUser, tweet);
                        isRetweeted = retweetRepository.existsByUserAndTweet(finalCurrentUser, tweet);
                    }
                    
                    return convertToTweetResponse(tweet, user, isLiked, isRetweeted);
                })
                .collect(Collectors.toList());
    }

    public TweetResponse getTweetById(Long id, String currentUsername) {
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + id));
        
        boolean isLiked = false;
        boolean isRetweeted = false;
        
        if (currentUsername != null && !currentUsername.isEmpty()) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));
            
            isLiked = likeRepository.existsByUserAndTweet(currentUser, tweet);
            isRetweeted = retweetRepository.existsByUserAndTweet(currentUser, tweet);
        }
        
        return convertToTweetResponse(tweet, tweet.getUser(), isLiked, isRetweeted);
    }

    @Transactional
    public TweetResponse updateTweet(Long id, TweetRequest tweetRequest, String username) {
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + id));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        if (!tweet.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this tweet");
        }
        
        tweet.setContent(tweetRequest.getContent());
        tweet.setImageUrl(tweetRequest.getImageUrl());
        
        Tweet updatedTweet = tweetRepository.save(tweet);
        
        boolean isLiked = likeRepository.existsByUserAndTweet(user, updatedTweet);
        boolean isRetweeted = retweetRepository.existsByUserAndTweet(user, updatedTweet);
        
        return convertToTweetResponse(updatedTweet, user, isLiked, isRetweeted);
    }

    @Transactional
    public void deleteTweet(Long id, String username) {
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + id));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        if (!tweet.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this tweet");
        }
        
        tweetRepository.delete(tweet);
    }
    
    private TweetResponse convertToTweetResponse(Tweet tweet, User user, boolean isLiked, boolean isRetweeted) {
        UserSummary userSummary = UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
        
        return TweetResponse.builder()
                .id(tweet.getId())
                .content(tweet.getContent())
                .imageUrl(tweet.getImageUrl())
                .user(userSummary)
                .commentsCount(tweet.getComments().size())
                .likesCount(tweet.getLikes().size())
                .retweetsCount(tweet.getRetweets().size())
                .isLiked(isLiked)
                .isRetweeted(isRetweeted)
                .createdAt(tweet.getCreatedAt())
                .updatedAt(tweet.getUpdatedAt())
                .build();
    }
} 