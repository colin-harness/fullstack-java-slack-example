import axios from 'axios';

const API_URL = '/api/auth';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

class AuthService {
  async login(username: string, password: string): Promise<LoginResponse> {
    const response = await axios.post(`${API_URL}/signin`, {
      username,
      password
    });
    return response.data;
  }

  async register(username: string, email: string, password: string): Promise<void> {
    await axios.post(`${API_URL}/signup`, {
      username,
      email,
      password
    });
  }

  async logout(): Promise<void> {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        await axios.post(`${API_URL}/signout`, {}, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } catch (error) {
        console.error('Logout error:', error);
      }
    }
  }

  async getCurrentUser(): Promise<User> {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('No token found');
    }

    const response = await axios.get('/api/user/me', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    return response.data;
  }

  getAuthHeader() {
    const token = localStorage.getItem('token');
    if (token) {
      return { Authorization: `Bearer ${token}` };
    }
    return {};
  }
}

export const authService = new AuthService();
