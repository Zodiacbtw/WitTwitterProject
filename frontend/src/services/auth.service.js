import axios from 'axios';

const API_URL = 'http://localhost:3000/';

const register = (username, email, password, bio, profileImageUrl) => {
  return axios.post(API_URL + 'register', {
    username,
    email,
    password,
    bio,
    profileImageUrl
  });
};

const login = (username, password) => {
  return axios
    .post(API_URL + 'login', {
      username,
      password,
    })
    .then((response) => {
      if (response.data.token) {
        // Tam yanıtı loglayalım
        console.log('Login response:', response.data);
        
        // Token'ı localStorage'a kaydedelim
        localStorage.setItem('user', JSON.stringify(response.data));
      }
      return response.data;
    });
};

const logout = () => {
  localStorage.removeItem('user');
};

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  if (!userStr) return null;
  
  try {
    const user = JSON.parse(userStr);
    console.log('Current user from localStorage:', user);
    
    // Token varlığını kontrol edelim
    if (!user.token) {
      console.error('Token not found in user object');
      return null;
    }
    return user;
  } catch (error) {
    console.error('Error parsing user from localStorage:', error);
    return null;
  }
};

const authService = {
  register,
  login,
  logout,
  getCurrentUser,
};

export default authService; 