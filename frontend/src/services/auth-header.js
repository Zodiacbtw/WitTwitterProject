export default function authHeader() {
  try {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      console.log('No user in localStorage');
      return {};
    }

    const user = JSON.parse(userStr);

    if (user && user.token) {
      console.log('Found auth token:', user.token.substring(0, 10) + '...');
      return { Authorization: `Bearer ${user.token}` };
    } else if (user && user.accessToken) {
      // Bazı JWT implementasyonları accessToken kullanır
      console.log('Found access token:', user.accessToken.substring(0, 10) + '...');
      return { Authorization: `Bearer ${user.accessToken}` };
    } else {
      console.log('User found but no token:', user);
      return {};
    }
  } catch (error) {
    console.error('Error creating auth header:', error);
    return {};
  }
} 