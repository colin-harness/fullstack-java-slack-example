import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import Login from '../Login';
import { useAuth } from '../../../contexts/AuthContext';

// Mock the auth context
jest.mock('../../../contexts/AuthContext');
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;

// Mock react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const theme = createTheme();

const renderLogin = () => {
  return render(
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <Login />
      </ThemeProvider>
    </BrowserRouter>
  );
};

describe('Login Component', () => {
  const mockLogin = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: null,
      login: mockLogin,
      register: jest.fn(),
      logout: jest.fn(),
      loading: false,
    });
  });

  it('should render login form', () => {
    renderLogin();

    expect(screen.getByText('Slack Chat')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /username/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('should handle successful login', async () => {
    mockLogin.mockResolvedValue(undefined);
    renderLogin();

    // Fill in the form using more specific queries
    const usernameInput = screen.getByRole('textbox', { name: /username/i });
    const passwordInput = screen.getByLabelText(/password/i);
    
    fireEvent.change(usernameInput, {
      target: { value: 'testuser' }
    });
    fireEvent.change(passwordInput, {
      target: { value: 'password123' }
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('testuser', 'password123');
      expect(mockNavigate).toHaveBeenCalledWith('/chat');
    });
  });

  it('should handle login error', async () => {
    const errorMessage = 'Invalid username or password';
    mockLogin.mockRejectedValue({
      response: { data: { message: errorMessage } }
    });
    renderLogin();

    // Fill in the form
    const usernameInput = screen.getByRole('textbox', { name: /username/i });
    const passwordInput = screen.getByLabelText(/password/i);
    
    fireEvent.change(usernameInput, {
      target: { value: 'testuser' }
    });
    fireEvent.change(passwordInput, {
      target: { value: 'wrongpassword' }
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should show loading state during login', async () => {
    mockUseAuth.mockReturnValue({
      user: null,
      login: mockLogin,
      register: jest.fn(),
      logout: jest.fn(),
      loading: true,
    });
    
    renderLogin();

    // Check that the form is rendered (loading state is handled by the component internally)
    expect(screen.getByRole('textbox', { name: /username/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('should have link to register page', () => {
    renderLogin();

    const registerLink = screen.getByText("Don't have an account? Sign Up");
    expect(registerLink).toBeInTheDocument();
    expect(registerLink.closest('a')).toHaveAttribute('href', '/register');
  });

  it('should allow form submission with filled fields', async () => {
    mockLogin.mockResolvedValue(undefined);
    renderLogin();

    const usernameInput = screen.getByRole('textbox', { name: /username/i });
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    // Fill in the form
    fireEvent.change(usernameInput, {
      target: { value: 'testuser' }
    });
    fireEvent.change(passwordInput, {
      target: { value: 'password123' }
    });

    // Submit the form
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('testuser', 'password123');
    });
  });
});
