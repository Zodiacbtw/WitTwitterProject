package com.twitter.api.repository;

import com.twitter.api.entity.Like;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByTweet(Tweet tweet);
    List<Like> findByUser(User user);
    Optional<Like> findByUserAndTweet(User user, Tweet tweet);
    boolean existsByUserAndTweet(User user, Tweet tweet);
    void deleteByUserAndTweet(User user, Tweet tweet);
} 