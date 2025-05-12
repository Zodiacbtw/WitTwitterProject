import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { Card, Button, Row, Col } from 'react-bootstrap';
import { AuthContext } from '../contexts/AuthContext';
import axios from 'axios';
import authHeader from '../services/auth-header';

const TweetItem = ({ tweet, onDelete, refreshTweets }) => {
  const { currentUser } = useContext(AuthContext);
  const [isLiked, setIsLiked] = useState(tweet.isLiked);
  const [likesCount, setLikesCount] = useState(tweet.likesCount);

  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  const handleLike = async () => {
    if (!currentUser) return;

    try {
      if (isLiked) {
        await axios.post(
          'http://localhost:3000/dislike',
          { tweetId: tweet.id },
          { headers: authHeader() }
        );
        setIsLiked(false);
        setLikesCount(likesCount - 1);
      } else {
        await axios.post(
          'http://localhost:3000/like',
          { tweetId: tweet.id },
          { headers: authHeader() }
        );
        setIsLiked(true);
        setLikesCount(likesCount + 1);
      }
    } catch (error) {
      console.error('Error toggling like:', error);
    }
  };

  return (
    <Card className="tweet-container mb-3">
      <Card.Body>
        <div className="d-flex justify-content-between">
          <div className="d-flex">
            <div>
              <Link to={`/profile/${tweet.user.id}`} className="text-decoration-none">
                <h5 className="mb-0">{tweet.user.username}</h5>
              </Link>
              <small className="text-muted">
                {formatDate(tweet.createdAt)}
              </small>
            </div>
          </div>
          {currentUser && currentUser.id === tweet.user.id && (
            <Button variant="outline-danger" size="sm" onClick={() => onDelete(tweet.id)}>
              Delete
            </Button>
          )}
        </div>
        <p className="mt-2">{tweet.content}</p>
        {tweet.imageUrl && (
          <div className="mt-2 mb-2">
            <img src={tweet.imageUrl} alt="Tweet" className="img-fluid rounded" />
          </div>
        )}
        <Row className="mt-3">
          <Col>
            <span
              className={`tweet-action ${isLiked ? 'active' : ''}`}
              onClick={handleLike}
            >
              <i className="bi bi-heart-fill"></i> {likesCount}
            </span>
            <span className="tweet-action">
              <i className="bi bi-chat"></i> {tweet.commentsCount}
            </span>
            <span className="tweet-action">
              <i className="bi bi-arrow-repeat"></i> {tweet.retweetsCount}
            </span>
          </Col>
        </Row>
      </Card.Body>
    </Card>
  );
};

export default TweetItem; 