import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Container, Row, Col, Alert, Button } from 'react-bootstrap';
import TweetForm from '../components/TweetForm';
import TweetItem from '../components/TweetItem'; // Bu dosyanın var olduğunu varsayıyorum
import { AuthContext } from '../contexts/AuthContext';
import tweetService from '../services/tweet.service';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/transitions.css'; // Bu dosyanın var olduğunu varsayıyorum

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentUser } = useContext(AuthContext);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAuthStatus, setShowAuthStatus] = useState(false);
  const [authMessage, setAuthMessage] = useState('');

  useEffect(() => {
    if (location.state && location.state.fromLogin && currentUser) {
      setAuthMessage(`Logged in as ${currentUser.username}`);
      setShowAuthStatus(true);
      const timer = setTimeout(() => {
        setShowAuthStatus(false);
        window.history.replaceState({}, document.title);
      }, 5000);
      return () => clearTimeout(timer);
    } else if (!currentUser) {
      setAuthMessage('Not logged in. Please login to post tweets.');
      setShowAuthStatus(false);
    }
  }, [currentUser, location.state]);

  const fetchFeed = useCallback(async () => { // Adını fetchFeed olarak değiştirdim
    setLoading(true);
    setError('');
    try {
      // Artık currentUser'a bağlı değil, tüm feed'i çekiyoruz
      const response = await tweetService.getTweetFeed(); // YENİ SERVİS ÇAĞRISI
      console.log('Home page - Tweet feed response:', response.data);
      setTweets(response.data);
    } catch (error) {
      console.error('Error fetching tweet feed:', error);
      setError(error.response?.data?.message || 'Failed to load tweet feed. Please try again later.');
    }
    setLoading(false);
  }, []); // Bağımlılık array'i boş, sadece mount'ta çalışır

  useEffect(() => {
    fetchFeed();
  }, [fetchFeed]);

  const handleDeleteTweet = async (id) => {
    try {
      await tweetService.deleteTweet(id);
      // fetchFeed(); // Veya state'i direkt güncelle
      setTweets(prevTweets => prevTweets.filter((tweet) => tweet.id !== id));
    } catch (error) {
      console.error('Error deleting tweet:', error);
      setError('Failed to delete tweet. Please try again later.');
    }
  };

  const handleTweetAdded = () => {
    fetchFeed(); // Yeni tweet eklendiğinde feed'i yenile
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
            <TweetForm onTweetAdded={handleTweetAdded} />
          ) : (
            <div className="text-center mb-4">
              <p>You need to be logged in to post tweets</p>
              <Button variant="primary" onClick={handleLogin}>
                Login
              </Button>
            </div>
          )}

          <h4 className="my-4">Home Feed</h4>

          {loading ? (
            <p className="text-center">Loading tweets...</p>
          ) : tweets.length === 0 ? (
            <p className="text-center">
              No tweets to display in the feed.
            </p>
          ) : (
            tweets.map((tweet) => (
              <TweetItem
                key={tweet.id}
                tweet={tweet}
                onDelete={handleDeleteTweet}
                refreshTweets={fetchFeed} // TweetItem'a refresh fonksiyonu geçirebiliriz
              />
            ))
          )}
        </Col>
      </Row>
    </Container>
  );
};

export default Home;