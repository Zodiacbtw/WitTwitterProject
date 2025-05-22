import axios from 'axios';
import authHeader from './auth-header';

// API_URL'in sonunda / olması, endpoint eklerken kolaylık sağlar.
// Eğer backend controller'ınızda @GetMapping("/feed") varsa,
// ve sınıf seviyesinde @RequestMapping("/tweet") varsa,
// tam yol http://localhost:3000/tweet/feed olur.
// API_URL'iniz zaten http://localhost:3000/tweet/ olduğu için bu iyi.
const API_URL = 'http://localhost:3000/tweet/';

const createTweet = (content, imageUrl) => {
  const tweetRequest = {
    content: content
  };
  if (imageUrl) {
    tweetRequest.imageUrl = imageUrl;
  }
  console.log('Sending createTweet request to:', API_URL, 'with data:', tweetRequest); // URL'i de loglayalım
  return axios.post(
    API_URL, // Bu POST /tweet/ adresine gider
    tweetRequest,
    { headers: authHeader() }
  );
};

const getTweetsByUserId = (userId) => {
  const url = `${API_URL}findByUserId?userId=${userId}`;
  console.log('Sending getTweetsByUserId request to:', url);
  return axios.get(url, { headers: authHeader() }); // Token göndermek iyi bir pratik olabilir
};

const getTweetById = (id) => {
  const url = `${API_URL}findById?id=${id}`;
  console.log('Sending getTweetById request to:', url);
  return axios.get(url, { headers: authHeader() }); // Token göndermek iyi bir pratik olabilir
};

// YENİ FONKSİYON
const getTweetFeed = () => {
  const url = `${API_URL}feed`; // http://localhost:3000/tweet/feed adresine istek yapar
  console.log('Sending getTweetFeed request to:', url);
  // Bu endpoint permitAll olsa da, token göndermek kullanıcının beğeni/retweet durumlarını getirebilir.
  return axios.get(url, { headers: authHeader() });
};

const updateTweet = (id, content, imageUrl) => {
  const tweetRequest = {
    content: content
  };
  if (imageUrl) {
    tweetRequest.imageUrl = imageUrl;
  }
  const url = `${API_URL}${id}`; // http://localhost:3000/tweet/{id}
  console.log('Sending updateTweet request to:', url, 'with data:', tweetRequest);
  return axios.put(
    url,
    tweetRequest,
    { headers: authHeader() }
  );
};

const deleteTweet = (id) => {
  const url = `${API_URL}${id}`; // http://localhost:3000/tweet/{id}
  console.log('Sending deleteTweet request to:', url);
  return axios.delete(url, { headers: authHeader() });
};

// Like, Dislike, Comment, Retweet servis fonksiyonları da buraya eklenebilir
// Örnek:
const likeTweet = (tweetId) => {
  const url = 'http://localhost:3000/like'; // Like endpoint'inizin tam yolu
  console.log('Sending likeTweet request to:', url, 'for tweetId:', tweetId);
  return axios.post(url, { tweetId }, { headers: authHeader() });
};

const dislikeTweet = (tweetId) => {
  const url = 'http://localhost:3000/dislike'; // Dislike endpoint'inizin tam yolu
  console.log('Sending dislikeTweet request to:', url, 'for tweetId:', tweetId);
  return axios.post(url, { tweetId }, { headers: authHeader() }); // Backend'iniz POST bekliyorsa
};

// ... (addComment, retweet fonksiyonları için de benzerleri)


const tweetService = {
  createTweet,
  getTweetsByUserId,
  getTweetById,
  getTweetFeed, // YENİ FONKSİYONU EKLEDİK
  updateTweet,
  deleteTweet,
  likeTweet,    // Örnek like fonksiyonunu ekledik
  dislikeTweet  // Örnek dislike fonksiyonunu ekledik
};

export default tweetService;