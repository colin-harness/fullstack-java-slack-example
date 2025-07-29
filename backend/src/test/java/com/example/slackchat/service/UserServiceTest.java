package com.example.slackchat.service;

import com.example.slackchat.model.User;
import com.example.slackchat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", ((User) result).getEmail());
    }

    @Test
    void loadUserByUsername_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, 
            () -> userService.loadUserByUsername("nonexistent"));
    }

    @Test
    void createUser_ValidData_CreatesUser() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser("newuser", "new@example.com", "password");

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password");
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.createUser("existinguser", "new@example.com", "password"));
        
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.createUser("newuser", "existing@example.com", "password"));
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void setUserOnlineStatus_UserExists_UpdatesStatus() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.setUserOnlineStatus("testuser", true);

        // Then
        verify(userRepository).save(testUser);
        assertTrue(testUser.isOnline());
    }
}
