import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Container, Row, Col, Alert } from 'react-bootstrap';
import TweetItem from './TweetItem';

const TweetList = ({ userId }) => {
    const [tweets, setTweets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchTweets = async () => {
            try {
                setLoading(true);
                const token = localStorage.getItem('token');
                
                const response = await axios.get(`http://localhost:3000/tweet/findByUserId`, {
                    params: { userId },
                    headers: token ? { Authorization: `Bearer ${token}` } : {}
                });
                
                setTweets(response.data);
                setError(null);
            } catch (err) {
                console.error('Error fetching tweets:', err);
                setError('Failed to load tweets. Please try again later.');
            } finally {
                setLoading(false);
            }
        };

        if (userId) {
            fetchTweets();
        }
    }, [userId]);

    const handleDeleteTweet = (deletedTweetId) => {
        setTweets(tweets.filter(tweet => tweet.id !== deletedTweetId));
    };

    return (
        <Container>
            <h2 className="my-4">User's Tweets</h2>
            
            {loading && <p>Loading tweets...</p>}
            
            {error && (
                <Alert variant="danger">
                    {error}
                </Alert>
            )}
            
            {!loading && !error && tweets.length === 0 && (
                <Alert variant="info">
                    No tweets found for this user.
                </Alert>
            )}
            
            <Row>
                {tweets.map(tweet => (
                    <Col md={12} key={tweet.id} className="mb-4">
                        <TweetItem 
                            tweet={tweet} 
                            onDelete={handleDeleteTweet}
                        />
                    </Col>
                ))}
            </Row>
        </Container>
    );
};

export default TweetList; 