package com.twitter.api.controller;

import com.twitter.api.dto.LikeRequest;
import com.twitter.api.dto.MessageResponse;
import com.twitter.api.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> likeTweet(@Valid @RequestBody LikeRequest likeRequest,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        MessageResponse response = likeService.likeTweet(likeRequest, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dislike")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> dislikeTweet(@Valid @RequestBody LikeRequest likeRequest,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        MessageResponse response = likeService.dislikeTweet(likeRequest, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
} 