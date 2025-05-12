package com.twitter.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetweetResponse {
    
    private Long id;
    private String comment;
    private UserSummary user;
    private TweetResponse originalTweet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 