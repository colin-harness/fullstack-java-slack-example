import axios from 'axios';
import { authService } from '../authService';

// Mock axios completely
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
}));

const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('AuthService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  describe('login', () => {
    it('should login successfully and return user data', async () => {
      const mockResponse = {
        data: {
          accessToken: 'mock-jwt-token',
          tokenType: 'Bearer',
          id: 1,
          username: 'testuser',
          email: 'test@example.com'
        }
      };

      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await authService.login('testuser', 'password123');

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/signin', {
        username: 'testuser',
        password: 'password123'
      });
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login error', async () => {
      const mockError = {
        response: {
          data: { message: 'Invalid credentials' }
        }
      };

      mockedAxios.post.mockRejectedValue(mockError);

      await expect(authService.login('testuser', 'wrongpassword'))
        .rejects.toEqual(mockError);
    });
  });

  describe('register', () => {
    it('should register successfully', async () => {
      const mockResponse = { data: { message: 'User registered successfully!' } };
      mockedAxios.post.mockResolvedValue(mockResponse);

      await authService.register('newuser', 'new@example.com', 'password123');

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/signup', {
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123'
      });
    });

    it('should handle registration error', async () => {
      const mockError = {
        response: {
          data: { message: 'Username already exists' }
        }
      };

      mockedAxios.post.mockRejectedValue(mockError);

      await expect(authService.register('existinguser', 'test@example.com', 'password123'))
        .rejects.toEqual(mockError);
    });
  });

  describe('logout', () => {
    it('should logout with token', async () => {
      localStorage.setItem('token', 'existing-token');
      mockedAxios.post.mockResolvedValue({ data: { message: 'Logged out' } });

      await authService.logout();

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/signout', {}, {
        headers: {
          Authorization: 'Bearer existing-token'
        }
      });
    });

    it('should handle logout without token', async () => {
      await authService.logout();

      expect(mockedAxios.post).not.toHaveBeenCalled();
    });

    it('should handle logout error gracefully', async () => {
      localStorage.setItem('token', 'existing-token');
      mockedAxios.post.mockRejectedValue(new Error('Network error'));

      // Should not throw error
      await expect(authService.logout()).resolves.toBeUndefined();
    });
  });

  describe('getCurrentUser', () => {
    it('should get current user when token exists', async () => {
      const mockUser = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com'
      };

      localStorage.setItem('token', 'valid-token');
      mockedAxios.get.mockResolvedValue({ data: mockUser });

      const result = await authService.getCurrentUser();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/user/me', {
        headers: {
          Authorization: 'Bearer valid-token'
        }
      });
      expect(result).toEqual(mockUser);
    });

    it('should throw error when no token exists', async () => {
      await expect(authService.getCurrentUser())
        .rejects.toThrow('No token found');
    });

    it('should handle request error', async () => {
      localStorage.setItem('token', 'invalid-token');
      const mockError = new Error('Unauthorized');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(authService.getCurrentUser())
        .rejects.toEqual(mockError);
    });
  });

  describe('getAuthHeader', () => {
    it('should return auth header with token', () => {
      localStorage.setItem('token', 'test-token');

      const result = authService.getAuthHeader();

      expect(result).toEqual({
        Authorization: 'Bearer test-token'
      });
    });

    it('should return empty object when no token', () => {
      const result = authService.getAuthHeader();

      expect(result).toEqual({});
    });
  });
});
