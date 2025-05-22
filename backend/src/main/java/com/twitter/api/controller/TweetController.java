package com.twitter.api.controller;

import com.twitter.api.dto.TweetRequest;
import com.twitter.api.dto.TweetResponse;
import com.twitter.api.service.TweetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tweet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TweetController {

    private static final Logger logger = LoggerFactory.getLogger(TweetController.class);

    @Autowired
    private TweetService tweetService;

    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> createTweet(@Valid @RequestBody TweetRequest tweetRequest,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("TweetController: createTweet (ORİJİNAL, @PostMapping('/')) CALLED. SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        if (userDetails != null) {
            logger.info("TweetController: UserDetails from @AuthenticationPrincipal. Username: {}, Authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());
        } else {
            logger.error("TweetController: UserDetails from @AuthenticationPrincipal is NULL despite @PreAuthorize! This is unexpected.");
        }
        if (userDetails == null) {
             logger.error("TweetController: createTweet (ORİJİNAL) - UserDetails is null, which is highly unexpected after @PreAuthorize. This will lead to an NPE.");
        }
        logger.info("TweetController: Attempting to call tweetService.createTweet for user: {}", (userDetails != null ? userDetails.getUsername() : "UNKNOWN (UserDetails is NULL)"));
        TweetResponse newTweet = tweetService.createTweet(tweetRequest, userDetails.getUsername());
        String responseAuthorUsername = "UNKNOWN_AUTHOR_IN_RESPONSE";
        if (newTweet != null && newTweet.getUser() != null && newTweet.getUser().getUsername() != null) {
            responseAuthorUsername = newTweet.getUser().getUsername();
        } else if (newTweet != null && newTweet.getUser() != null) {
            logger.warn("TweetController: TweetResponse's UserSummary has a null username.");
            responseAuthorUsername = "USER_SUMMARY_HAS_NULL_USERNAME";
        } else if (newTweet != null) {
            logger.warn("TweetController: TweetResponse has a null UserSummary object.");
            responseAuthorUsername = "USER_SUMMARY_IS_NULL";
        }
        Long responseTweetId = (newTweet != null && newTweet.getId() != null) ? newTweet.getId() : null;
        logger.info("TweetController: Tweet creation process completed. Requesting user: {}. Response Tweet ID: {}, Tweet Author in Response DTO: {}",
                (userDetails != null ? userDetails.getUsername() : "UNKNOWN_REQUESTING_USER"),
                responseTweetId,
                responseAuthorUsername);
        return new ResponseEntity<>(newTweet, HttpStatus.CREATED);
    }

    // YENİ ENDPOINT (Home Page için Tüm Tweetleri Getirmek Üzere)
    @GetMapping("/feed")
    public ResponseEntity<List<TweetResponse>> getTweetFeed(@AuthenticationPrincipal UserDetails userDetails) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        // Log: Bu endpoint çağrıldığında logla
        logger.info("TweetController: getTweetFeed CALLED. Authenticated user (if any): {}", currentUsername);
        List<TweetResponse> tweets = tweetService.getAllTweetsChronologically(currentUsername);
        logger.info("TweetController: getTweetFeed returning {} tweets.", tweets.size());
        return ResponseEntity.ok(tweets);
    }


    @GetMapping("/findByUserId")
    public ResponseEntity<List<TweetResponse>> getTweetsByUserId(@RequestParam Long userId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        String usernameForService = userDetails != null ? userDetails.getUsername() : null;
        logger.info("TweetController: getTweetsByUserId CALLED for userId: {}. Authenticated user (if any): {}", userId, usernameForService);
        List<TweetResponse> tweets = tweetService.getTweetsByUserId(userId, usernameForService);
        return ResponseEntity.ok(tweets);
    }

    @GetMapping("/findById")
    public ResponseEntity<TweetResponse> getTweetById(@RequestParam Long id,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        String usernameForService = userDetails != null ? userDetails.getUsername() : null;
        logger.info("TweetController: getTweetById CALLED for tweetId: {}. Authenticated user (if any): {}", id, usernameForService);
        TweetResponse tweet = tweetService.getTweetById(id, usernameForService);
        return ResponseEntity.ok(tweet);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> updateTweet(@PathVariable Long id,
                                                    @Valid @RequestBody TweetRequest tweetRequest,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("TweetController: updateTweet CALLED for tweetId: {}. SecurityContext Authentication: {}", id, SecurityContextHolder.getContext().getAuthentication());
        if (userDetails != null) {
            logger.info("TweetController: UserDetails from @AuthenticationPrincipal for update: {}", userDetails.getUsername());
        } else {
            logger.error("TweetController: updateTweet - UserDetails from @AuthenticationPrincipal is NULL!");
        }
        if (userDetails == null) {
             logger.error("TweetController: updateTweet - UserDetails is null, cannot proceed.");
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("TweetController: Attempting to call tweetService.updateTweet for tweetId: {} by user: {}", id, userDetails.getUsername());
        TweetResponse updatedTweet = tweetService.updateTweet(id, tweetRequest, userDetails.getUsername());
        logger.info("TweetController: Tweet updated successfully for tweetId: {}", id);
        return ResponseEntity.ok(updatedTweet);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteTweet(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("TweetController: deleteTweet CALLED for tweetId: {}. SecurityContext Authentication: {}", id, SecurityContextHolder.getContext().getAuthentication());
        if (userDetails != null) {
            logger.info("TweetController: UserDetails from @AuthenticationPrincipal for delete: {}", userDetails.getUsername());
        } else {
            logger.error("TweetController: deleteTweet - UserDetails from @AuthenticationPrincipal is NULL!");
        }
        if (userDetails == null) {
             logger.error("TweetController: deleteTweet - UserDetails is null, cannot proceed.");
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("TweetController: Attempting to call tweetService.deleteTweet for tweetId: {} by user: {}", id, userDetails.getUsername());
        tweetService.deleteTweet(id, userDetails.getUsername());
        logger.info("TweetController: Tweet deleted successfully for tweetId: {}", id);
        return ResponseEntity.noContent().build();
    }
}