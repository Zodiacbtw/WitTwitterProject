package com.twitter.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TwitterApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitterApiApplication.class, args);
    }
} 