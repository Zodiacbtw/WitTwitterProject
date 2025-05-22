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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException importu
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service // Bu anotasyon önemli
public class TweetService {

    private static final Logger logger = LoggerFactory.getLogger(TweetService.class);

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository; // TweetResponse için gerekli

    @Autowired
    private RetweetRepository retweetRepository; // TweetResponse için gerekli

    @Transactional
    public TweetResponse createTweet(TweetRequest tweetRequest, String username) {
        logger.info("TweetService: createTweet CALLED for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Tweet tweet = Tweet.builder()
                .content(tweetRequest.getContent())
                .imageUrl(tweetRequest.getImageUrl())
                .user(user)
                .build(); // Lombok @Builder.Default sayesinde comments, likes, retweets initialize edilecek

        Tweet savedTweet = tweetRepository.save(tweet);
        logger.info("TweetService: Tweet saved with ID: {}", savedTweet.getId());

        // Yeni oluşturulan tweet için isLiked ve isRetweeted false olacaktır.
        return convertToTweetResponse(savedTweet, user, false, false);
    }

    public List<TweetResponse> getAllTweetsChronologically(String currentUsername) {
        logger.info("TweetService: getAllTweetsChronologically CALLED by user (if any): {}", currentUsername);
        User currentUser = null;
        if (currentUsername != null && !currentUsername.isEmpty()) {
            currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        }
        final User finalCurrentUser = currentUser;

        List<Tweet> allTweets = tweetRepository.findAllByOrderByCreatedAtDesc();
        if (allTweets.isEmpty()) {
            return Collections.emptyList();
        }
        return allTweets.stream()
                .map(tweet -> convertToTweetResponse(tweet, tweet.getUser(),
                        finalCurrentUser != null && likeRepository.existsByUserAndTweet(finalCurrentUser, tweet),
                        finalCurrentUser != null && retweetRepository.existsByUserAndTweet(finalCurrentUser, tweet)))
                .collect(Collectors.toList());
    }

    public List<TweetResponse> getTweetsByUserId(Long userId, String currentUsername) {
        logger.info("TweetService: getTweetsByUserId CALLED for userId: {}. Requesting user (if any): {}", userId, currentUsername);
        User userWhoseTweetsToFetch = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User currentUser = null;
        if (currentUsername != null && !currentUsername.isEmpty()) {
            currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        }
        final User finalCurrentUser = currentUser;

        List<Tweet> userTweets = tweetRepository.findByUserOrderByCreatedAtDesc(userWhoseTweetsToFetch);
         if (userTweets.isEmpty()) {
            return Collections.emptyList();
        }
        return userTweets.stream()
                .map(tweet -> convertToTweetResponse(tweet, userWhoseTweetsToFetch, // Tweetin sahibi userWhoseTweetsToFetch
                        finalCurrentUser != null && likeRepository.existsByUserAndTweet(finalCurrentUser, tweet),
                        finalCurrentUser != null && retweetRepository.existsByUserAndTweet(finalCurrentUser, tweet)))
                .collect(Collectors.toList());
    }

    public TweetResponse getTweetById(Long id, String currentUsername) {
        logger.info("TweetService: getTweetById CALLED for tweetId: {}. Requesting user (if any): {}", id, currentUsername);
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + id));

        User currentUser = null;
        if (currentUsername != null && !currentUsername.isEmpty()) {
            currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        }

        boolean isLiked = currentUser != null && likeRepository.existsByUserAndTweet(currentUser, tweet);
        boolean isRetweeted = currentUser != null && retweetRepository.existsByUserAndTweet(currentUser, tweet);

        return convertToTweetResponse(tweet, tweet.getUser(), isLiked, isRetweeted);
    }

    @Transactional
    public TweetResponse updateTweet(Long id, TweetRequest tweetRequest, String username) {
        logger.info("TweetService: updateTweet CALLED for tweetId: {} by user: {}", id, username);
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
        logger.info("TweetService: deleteTweet CALLED for tweetId: {} by user: {}", id, username);
        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet not found with id: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (!tweet.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this tweet");
        }
        tweetRepository.delete(tweet);
    }

    // Bu metot, Tweet entity'sini TweetResponse DTO'suna dönüştürür.
    // Tweet'in sahibi olan User objesi de parametre olarak alınır.
    private TweetResponse convertToTweetResponse(Tweet tweet, User tweetOwner, boolean isLiked, boolean isRetweeted) {
        if (tweetOwner == null) {
            logger.error("convertToTweetResponse: tweetOwner is null for tweet ID: {}. Using a placeholder.", tweet.getId());
            // Güvenlik veya tutarlılık açısından bu durumun nasıl ele alınacağına karar verilmeli.
            // Şimdilik, UserSummary oluşturulurken hata almamak için bir placeholder oluşturalım.
            tweetOwner = User.builder().id(-1L).username("unknown").email("unknown").bio("").profileImageUrl("").build();
        }
        UserSummary userSummary = UserSummary.builder()
                .id(tweetOwner.getId())
                .username(tweetOwner.getUsername())
                .email(tweetOwner.getEmail()) // UserSummary'de email varsa
                .bio(tweetOwner.getBio())
                .profileImageUrl(tweetOwner.getProfileImageUrl())
                .build();

        return TweetResponse.builder()
                .id(tweet.getId())
                .content(tweet.getContent())
                .imageUrl(tweet.getImageUrl())
                .user(userSummary)
                .commentsCount(tweet.getComments() != null ? tweet.getComments().size() : 0)
                .likesCount(tweet.getLikes() != null ? tweet.getLikes().size() : 0)
                .retweetsCount(tweet.getRetweets() != null ? tweet.getRetweets().size() : 0)
                .isLiked(isLiked)
                .isRetweeted(isRetweeted)
                .createdAt(tweet.getCreatedAt())
                .updatedAt(tweet.getUpdatedAt())
                .build();
    }
}