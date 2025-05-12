import React, { useState } from 'react';
import { Container, Form, Button, Card } from 'react-bootstrap';
import TweetList from '../components/TweetList';
import Navigation from '../components/Navigation';

const UserTweetsPage = () => {
    const [userId, setUserId] = useState('');
    const [search, setSearch] = useState(false);
    
    const handleSubmit = (e) => {
        e.preventDefault();
        setSearch(true);
    };
    
    return (
        <>
            <Navigation />
            <Container className="mt-5">
                <Card>
                    <Card.Header as="h5">User Tweet Search</Card.Header>
                    <Card.Body>
                        <Form onSubmit={handleSubmit}>
                            <Form.Group className="mb-3">
                                <Form.Label>Enter User ID</Form.Label>
                                <Form.Control 
                                    type="number" 
                                    placeholder="User ID" 
                                    value={userId}
                                    onChange={(e) => setUserId(e.target.value)}
                                    required
                                />
                                <Form.Text className="text-muted">
                                    Enter the ID of the user whose tweets you want to view.
                                </Form.Text>
                            </Form.Group>
                            <Button variant="primary" type="submit">
                                Search Tweets
                            </Button>
                        </Form>
                    </Card.Body>
                </Card>
                
                {search && userId && (
                    <div className="mt-4">
                        <TweetList userId={userId} />
                    </div>
                )}
            </Container>
        </>
    );
};

export default UserTweetsPage; 