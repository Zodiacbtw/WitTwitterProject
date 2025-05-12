import React, { useState } from 'react';
import { Form, Button } from 'react-bootstrap';
import tweetService from '../services/tweet.service';

const TweetForm = ({ onTweetAdded }) => {
  const [content, setContent] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;

    setLoading(true);
    try {
      await tweetService.createTweet(content.trim(), imageUrl.trim() || null);
      setContent('');
      setImageUrl('');
      
      if (onTweetAdded) {
        onTweetAdded();
      }
    } catch (error) {
      console.error('Error creating tweet:', error);
    }
    setLoading(false);
  };

  return (
    <div className="tweet-container">
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