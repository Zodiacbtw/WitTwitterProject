package com.twitter.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RetweetRequest {
    
    @NotNull
    private Long tweetId;
    
    @Size(max = 280)
    private String comment;
} 