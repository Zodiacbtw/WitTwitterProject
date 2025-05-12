import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Alert } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import TweetItem from '../components/TweetItem';
import tweetService from '../services/tweet.service';

const Profile = () => {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUserTweets = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await tweetService.getTweetsByUserId(id);
        if (response.data.length > 0) {
          setUser(response.data[0].user);
        }
        setTweets(response.data);
        setLoading(false);
      } catch (err) {
        console.error(err);
        setError('Failed to load user profile. Please try again later.');
        setLoading(false);
      }
    };

    fetchUserTweets();
  }, [id]);

  const handleDeleteTweet = async (id) => {
    try {
      await tweetService.deleteTweet(id);
      setTweets(tweets.filter((tweet) => tweet.id !== id));
    } catch (error) {
      console.error('Error deleting tweet:', error);
      setError('Failed to delete tweet. Please try again later.');
    }
  };

  if (loading) {
    return <div className="text-center mt-5">Loading profile...</div>;
  }

  if (error) {
    return (
      <Container className="mt-4">
        <Alert variant="danger">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container className="mt-4">
      {user ? (
        <>
          <Card className="profile-header">
            <Row>
              <Col md={3} className="text-center">
                {user.profileImageUrl ? (
                  <img
                    src={user.profileImageUrl}
                    alt={user.username}
                    className="profile-image"
                  />
                ) : (
                  <div
                    className="profile-image d-flex align-items-center justify-content-center bg-secondary text-white"
                    style={{ fontSize: '2rem' }}
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
                  <strong>{tweets.length}</strong> Tweets
                </p>
              </Col>
            </Row>
          </Card>

          <h4 className="mb-4">Tweets</h4>
          {tweets.length === 0 ? (
            <p className="text-center">This user hasn't posted any tweets yet.</p>
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
        <Alert variant="warning">User not found</Alert>
      )}
    </Container>
  );
};

export default Profile; 