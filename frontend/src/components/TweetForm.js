import React, { useState } from 'react';
import { Form, Button, Alert } from 'react-bootstrap';
import tweetService from '../services/tweet.service';

const TweetForm = ({ onTweetAdded }) => {
  const [content, setContent] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;

    setLoading(true);
    setError('');
    setSuccess(false);
    
    try {
      const response = await tweetService.createTweet(content.trim(), imageUrl.trim() || null);
      console.log('Tweet created successfully:', response);
      setContent('');
      setImageUrl('');
      setSuccess(true);
      
      if (onTweetAdded) {
        onTweetAdded();
      }
    } catch (error) {
      console.error('Error creating tweet:', error);
      setError(
        error.response?.data?.message || 
        error.message || 
        'Failed to create tweet. Make sure you are logged in.'
      );
    }
    setLoading(false);
  };

  return (
    <div className="tweet-container">
      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">Tweet posted successfully!</Alert>}
      
      <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3">
          <Form.Control
            as="textarea"
            rows={3}
            placeholder="What's happening?"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            maxLength={280}
          />
          <div className="text-end text-muted mt-1">
            <small>{content.length}/280</small>
          </div>
        </Form.Group>
        <Form.Group className="mb-3">
          <Form.Control
            type="text"
            placeholder="Image URL (optional)"
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
          />
        </Form.Group>
        <div className="text-end">
          <Button
            type="submit"
            className="btn-twitter"
            disabled={!content.trim() || loading}
          >
            {loading ? 'Posting...' : 'Tweet'}
          </Button>
        </div>
      </Form>
    </div>
  );
};

export default TweetForm; 