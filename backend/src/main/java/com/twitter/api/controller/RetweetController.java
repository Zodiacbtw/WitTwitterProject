package com.twitter.api.controller;

import com.twitter.api.dto.RetweetRequest;
import com.twitter.api.dto.RetweetResponse;
import com.twitter.api.service.RetweetService;
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
@RequestMapping("/retweet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RetweetController {

    @Autowired
    private RetweetService retweetService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RetweetResponse> retweetTweet(@Valid @RequestBody RetweetRequest retweetRequest,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        RetweetResponse newRetweet = retweetService.retweetTweet(retweetRequest, userDetails.getUsername());
        return new ResponseEntity<>(newRetweet, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RetweetResponse>> getRetweetsByUsername(@AuthenticationPrincipal UserDetails userDetails) {
        List<RetweetResponse> retweets = retweetService.getRetweetsByUsername(userDetails.getUsername());
        return ResponseEntity.ok(retweets);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteRetweet(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        retweetService.deleteRetweet(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
} 