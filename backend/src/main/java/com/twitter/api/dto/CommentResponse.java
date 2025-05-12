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
public class CommentResponse {
    
    private Long id;
    private String content;
    private UserSummary user;
    private Long tweetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 