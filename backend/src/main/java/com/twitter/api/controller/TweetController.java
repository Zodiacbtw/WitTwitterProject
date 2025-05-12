package com.twitter.api.controller;

import com.twitter.api.dto.TweetRequest;
import com.twitter.api.dto.TweetResponse;
import com.twitter.api.service.TweetService;
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
@RequestMapping("/tweet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TweetController {

    @Autowired
    private TweetService tweetService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> createTweet(@Valid @RequestBody TweetRequest tweetRequest,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        TweetResponse newTweet = tweetService.createTweet(tweetRequest, userDetails.getUsername());
        return new ResponseEntity<>(newTweet, HttpStatus.CREATED);
    }

    @GetMapping("/findByUserId")
    public ResponseEntity<List<TweetResponse>> getTweetsByUserId(@RequestParam Long userId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        List<TweetResponse> tweets = tweetService.getTweetsByUserId(userId, username);
        return ResponseEntity.ok(tweets);
    }

    @GetMapping("/findById")
    public ResponseEntity<TweetResponse> getTweetById(@RequestParam Long id,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        TweetResponse tweet = tweetService.getTweetById(id, username);
        return ResponseEntity.ok(tweet);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> updateTweet(@PathVariable Long id,
                                                    @Valid @RequestBody TweetRequest tweetRequest,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        TweetResponse updatedTweet = tweetService.updateTweet(id, tweetRequest, userDetails.getUsername());
        return ResponseEntity.ok(updatedTweet);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteTweet(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        tweetService.deleteTweet(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
} 