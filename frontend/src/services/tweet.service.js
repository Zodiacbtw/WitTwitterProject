import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://localhost:3000/tweet/';

const createTweet = (content, imageUrl) => {
  return axios.post(
    API_URL,
    { content, imageUrl },
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
  return axios.put(
    API_URL + id,
    { content, imageUrl },
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