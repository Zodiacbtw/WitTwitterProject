import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Container, Row, Col, Alert, Button } from 'react-bootstrap';
import TweetForm from '../components/TweetForm';
import TweetItem from '../components/TweetItem';
import { AuthContext } from '../contexts/AuthContext';
import tweetService from '../services/tweet.service';
import { useNavigate } from 'react-router-dom';

const Home = () => {
  const navigate = useNavigate();
  const { currentUser } = useContext(AuthContext);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [authStatus, setAuthStatus] = useState('');

  // Auth durumunu kontrol et
  useEffect(() => {
    if (currentUser) {
      const tokenDetails = `Token: ${currentUser.token ? 'Available' : 'Missing'}`;
      setAuthStatus(`Logged in as ${currentUser.username}. ${tokenDetails}`);
    } else {
      setAuthStatus('Not logged in. Please login to post tweets.');
    }
  }, [currentUser]);

  const fetchTweets = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      if (currentUser) {
        const response = await tweetService.getTweetsByUserId(currentUser.id);
        setTweets(response.data);
      } else {
        setTweets([]);
      }
    } catch (error) {
      console.error('Error fetching tweets:', error);
      setError('Failed to load tweets. Please try again later.');
    }
    setLoading(false);
  }, [currentUser]);

  useEffect(() => {
    fetchTweets();
  }, [fetchTweets]);

  const handleDeleteTweet = async (id) => {
    try {
      await tweetService.deleteTweet(id);
      setTweets(tweets.filter((tweet) => tweet.id !== id));
    } catch (error) {
      console.error('Error deleting tweet:', error);
      setError('Failed to delete tweet. Please try again later.');
    }
  };

  const handleLogin = () => {
    navigate('/login');
  };

  return (
    <Container>
      {error && <Alert variant="danger">{error}</Alert>}
      {authStatus && <Alert variant="info">{authStatus}</Alert>}
      
      <Row>
        <Col md={8} className="mx-auto">
          {currentUser ? (
            <TweetForm onTweetAdded={fetchTweets} />
          ) : (
            <div className="text-center mb-4">
              <p>You need to be logged in to post tweets</p>
              <Button variant="primary" onClick={handleLogin}>
                Login
              </Button>
            </div>
          )}

          <h4 className="my-4">Recent Tweets</h4>
          
          {loading ? (
            <p className="text-center">Loading tweets...</p>
          ) : tweets.length === 0 ? (
            <p className="text-center">
              {currentUser 
                ? "You haven't posted any tweets yet." 
                : "Please log in to see tweets."}
            </p>
          ) : (
            tweets.map((tweet) => (
              <TweetItem
                key={tweet.id}
                tweet={tweet}
                onDelete={handleDeleteTweet}
                refreshTweets={fetchTweets}
              />
            ))
          )}
        </Col>
      </Row>
    </Container>
  );
};

export default Home; 