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
public class TweetResponse {
    
    private Long id;
    private String content;
    private String imageUrl;
    private UserSummary user;
    private int commentsCount;
    private int likesCount;
    private int retweetsCount;
    private boolean isLiked;
    private boolean isRetweeted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 