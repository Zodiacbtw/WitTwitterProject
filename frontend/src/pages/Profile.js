import React, { useState, useEffect, useContext } from 'react';
import { Container, Row, Col, Card, Alert, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import TweetItem from '../components/TweetItem';
import tweetService from '../services/tweet.service';
import { AuthContext } from '../contexts/AuthContext';

const Profile = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useContext(AuthContext);
  const [user, setUser] = useState(null);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUserProfile = async () => {
      setLoading(true);
      setError('');
      
      try {
        // Doğrudan localStorage'dan kullanıcı bilgilerini al
        if (currentUser && currentUser.id.toString() === id) {
          setUser({
            id: currentUser.id,
            username: currentUser.username,
            email: currentUser.email,
            // Eğer bio ve profileImageUrl varsa onları da ekle
            bio: currentUser.bio || '',
            profileImageUrl: currentUser.profileImageUrl || ''
          });
        } else {
          // Kullanıcı bilgilerini tweet'lerden çekmeye çalış
          const response = await tweetService.getTweetsByUserId(id);
          if (response.data.length > 0) {
            setUser(response.data[0].user);
          } else {
            setError('User has no tweets. Profile information is limited.');
          }
        }
        
        // Her durumda tweet'leri getir
        const tweetResponse = await tweetService.getTweetsByUserId(id);
        setTweets(tweetResponse.data);
      } catch (err) {
        console.error('Error loading profile:', err);
        setError('Failed to load user profile. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, [id, currentUser]);

  const handleDeleteTweet = async (id) => {
    try {
      await tweetService.deleteTweet(id);
      setTweets(tweets.filter((tweet) => tweet.id !== id));
    } catch (error) {
      console.error('Error deleting tweet:', error);
      setError('Failed to delete tweet. Please try again later.');
    }
  };

  const handleGoBack = () => {
    navigate(-1);
  };

  if (loading) {
    return (
      <Container className="text-center mt-5">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <p className="mt-2">Loading profile...</p>
      </Container>
    );
  }

  return (
    <Container className="mt-4">
      {error && <Alert variant="warning">{error}</Alert>}
      
      {user ? (
        <>
          <Card className="profile-header mb-4">
            <Card.Body>
              <Row>
                <Col md={3} className="text-center">
                  {user.profileImageUrl ? (
                    <img
                      src={user.profileImageUrl}
                      alt={user.username}
                      className="profile-image rounded-circle"
                      style={{ width: '150px', height: '150px', objectFit: 'cover' }}
                    />
                  ) : (
                    <div
                      className="profile-image d-flex align-items-center justify-content-center bg-secondary text-white rounded-circle mx-auto"
                      style={{ width: '150px', height: '150px', fontSize: '4rem' }}
                    >
                      {user.username.charAt(0).toUpperCase()}
                    </div>
                  )}
                </Col>
                <Col md={9}>
                  <h3>{user.username}</h3>
                  <p className="text-muted">{user.email}</p>
                  {user.bio && <p>{user.bio}</p>}
                  <p className="text-muted">
                    <strong>{tweets.length}</strong> {tweets.length === 1 ? 'Tweet' : 'Tweets'}
                  </p>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <h4 className="mb-4">Tweets</h4>
          {tweets.length === 0 ? (
            <Alert variant="info">This user hasn't posted any tweets yet.</Alert>
          ) : (
            tweets.map((tweet) => (
              <TweetItem
                key={tweet.id}
                tweet={tweet}
                onDelete={handleDeleteTweet}
              />
            ))
          )}
        </>
      ) : (
        <div className="text-center">
          <Alert variant="warning" className="mb-4">
            User not found or profile information is unavailable.
          </Alert>
          <button 
            className="btn btn-outline-primary"
            onClick={handleGoBack}
          >
            Go Back
          </button>
        </div>
      )}
    </Container>
  );
};

export default Profile; 