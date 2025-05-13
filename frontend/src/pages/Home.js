import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Container, Row, Col, Alert, Button } from 'react-bootstrap';
import TweetForm from '../components/TweetForm';
import TweetItem from '../components/TweetItem';
import { AuthContext } from '../contexts/AuthContext';
import tweetService from '../services/tweet.service';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/transitions.css';

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentUser } = useContext(AuthContext);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAuthStatus, setShowAuthStatus] = useState(false);
  const [authMessage, setAuthMessage] = useState('');

  // Auth durumunu kontrol et - sadece login işleminden sonra bildirim göster
  useEffect(() => {
    // Sadece state'te 'fromLogin' parametresi varsa bildirim göster
    if (location.state && location.state.fromLogin && currentUser) {
      setAuthMessage(`Logged in as ${currentUser.username}`);
      setShowAuthStatus(true);
      
      // 5 saniye sonra mesajı kaldır
      const timer = setTimeout(() => {
        setShowAuthStatus(false);
        // State'i temizle ki yeniden yönlendirmelerde bildirim tekrar gösterilmesin
        window.history.replaceState({}, document.title);
      }, 5000);
      
      return () => clearTimeout(timer);
    } else if (!currentUser) {
      setAuthMessage('Not logged in. Please login to post tweets.');
      setShowAuthStatus(false); // Normal durumda gizli olsun
    }
  }, [currentUser, location.state]);

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
      
      {showAuthStatus && (
        <div className={`auth-notification ${showAuthStatus ? 'show' : 'hide'}`}>
          <Alert variant="info" className="mb-0">
            {authMessage}
          </Alert>
        </div>
      )}
      
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