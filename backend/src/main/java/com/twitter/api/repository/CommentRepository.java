package com.twitter.api.repository;

import com.twitter.api.entity.Comment;
import com.twitter.api.entity.Tweet;
import com.twitter.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTweetOrderByCreatedAtDesc(Tweet tweet);
    List<Comment> findByUser(User user);
    Optional<Comment> findByIdAndUser(Long id, User user);
    Optional<Comment> findByIdAndTweetUser(Long id, User tweetUser);
} 