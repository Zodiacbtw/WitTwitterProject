import React, { useState, useContext } from 'react';
import { Container, Form, Button, Card, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../contexts/AuthContext';
import AuthService from '../services/auth.service';

const Login = () => {
  const navigate = useNavigate();
  const { setCurrentUser } = useContext(AuthContext);
  
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);

    try {
      const response = await AuthService.login(username, password);
      setCurrentUser(response);
      navigate('/');
    } catch (error) {
      const resMessage =
        (error.response &&
          error.response.data &&
          error.response.data.message) ||
        error.message ||
        error.toString();
      setMessage(resMessage);
      setLoading(false);
    }
  };

  return (
    <Container>
      <div className="d-flex justify-content-center">
        <Card style={{ width: '400px' }} className="mt-4">
          <Card.Body>
            <Card.Title className="text-center mb-4">Login</Card.Title>

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Username</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Enter username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  placeholder="Enter password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </Form.Group>

              {message && (
                <Alert variant="danger" className="mt-3">
                  {message}
                </Alert>
              )}

              <Button
                variant="primary"
                type="submit"
                className="w-100 mt-3 btn-twitter"
                disabled={loading}
              >
                {loading ? 'Loading...' : 'Sign In'}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      </div>
    </Container>
  );
};

export default Login; 