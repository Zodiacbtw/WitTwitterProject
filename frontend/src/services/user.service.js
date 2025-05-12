import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://localhost:3000/user/';

// Kullanıcı bilgilerini ID'ye göre getir
const getUserById = (id) => {
  return axios.get(API_URL + id, { headers: authHeader() });
};

// Mevcut kullanıcı bilgilerini getir (JWT token'dan)
const getCurrentUserDetails = () => {
  return axios.get(API_URL + 'me', { headers: authHeader() });
};

const userService = {
  getUserById,
  getCurrentUserDetails
};

export default userService; 