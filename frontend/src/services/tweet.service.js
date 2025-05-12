import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://localhost:3000/tweet/';

const createTweet = (content, imageUrl) => {
  const tweetRequest = {
    content: content
  };
  
  if (imageUrl) {
    tweetRequest.imageUrl = imageUrl;
  }
  
  console.log('Sending tweet request:', tweetRequest);
  console.log('Auth headers:', authHeader());
  
  return axios.post(
    API_URL,
    tweetRequest,
    { headers: authHeader() }
  );
};

const getTweetsByUserId = (userId) => {
  return axios.get(API_URL + 'findByUserId?userId=' + userId);
};

const getTweetById = (id) => {
  return axios.get(API_URL + 'findById?id=' + id);
};

const updateTweet = (id, content, imageUrl) => {
  const tweetRequest = {
    content: content
  };
  
  if (imageUrl) {
    tweetRequest.imageUrl = imageUrl;
  }
  
  return axios.put(
    API_URL + id,
    tweetRequest,
    { headers: authHeader() }
  );
};

const deleteTweet = (id) => {
  return axios.delete(API_URL + id, { headers: authHeader() });
};

const tweetService = {
  createTweet,
  getTweetsByUserId,
  getTweetById,
  updateTweet,
  deleteTweet,
};

export default tweetService; 