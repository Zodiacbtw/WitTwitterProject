package com.twitter.api.repository;

import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {
    List<Tweet> findByUser(User user); // Bu metot var, kalsın.
    List<Tweet> findByUserOrderByCreatedAtDesc(User user);
    List<Tweet> findByUserIdOrderByCreatedAtDesc(Long userId); // Bu da var, kalsın.
    List<Tweet> findAllByOrderByCreatedAtDesc(); // YENİ METOT
}