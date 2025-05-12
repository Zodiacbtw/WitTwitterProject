package com.twitter.api.repository;

import com.twitter.api.entity.Retweet;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetweetRepository extends JpaRepository<Retweet, Long> {
    List<Retweet> findByTweet(Tweet tweet);
    List<Retweet> findByUser(User user);
    Optional<Retweet> findByUserAndTweet(User user, Tweet tweet);
    boolean existsByUserAndTweet(User user, Tweet tweet);
    void deleteByUserAndTweet(User user, Tweet tweet);
} 