package com.twitter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TweetRequest {
    
    @NotBlank
    @Size(max = 280)
    private String content;
    
    @Size(max = 200)
    private String imageUrl;
} 