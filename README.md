# Twitter Clone Project

This project is a Twitter clone built with Spring Boot (backend) and React (frontend).

## Features

- User registration and authentication
- Create, read, update, and delete tweets
- Like/dislike tweets
- Comment on tweets
- Retweet functionality
- User profiles

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.1.3
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Maven

### Frontend

- React 18
- React Router Dom
- Axios
- Bootstrap 5
- React Bootstrap

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js and npm
- PostgreSQL database

### Database Setup

1. Create a PostgreSQL database named `twitter_db`
2. Default credentials are:
   - Username: postgres
   - Password: postgres
   - You can modify these in `backend/src/main/resources/application.properties`

### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will run on http://localhost:3000

### Frontend Setup

```bash
cd frontend
npm install
npm start
```

The frontend will run on http://localhost:3200

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login a user

### Tweets
- `POST /tweet` - Create a new tweet
- `GET /tweet/findByUserId` - Get tweets by user ID
- `GET /tweet/findById` - Get tweet by ID
- `PUT /tweet/:id` - Update a tweet
- `DELETE /tweet/:id` - Delete a tweet

### Comments
- `POST /comment` - Add a comment to a tweet
- `GET /comment/tweet/:tweetId` - Get comments for a tweet
- `PUT /comment/:id` - Update a comment
- `DELETE /comment/:id` - Delete a comment

### Likes/Dislikes
- `POST /like` - Like a tweet
- `POST /dislike` - Dislike a tweet

### Retweets
- `POST /retweet` - Retweet a tweet
- `DELETE /retweet/:id` - Delete a retweet

## Notes

- CORS is configured to allow requests from http://localhost:3200 (frontend) to http://localhost:3000 (backend)
- JWT is used for authentication and authorization 