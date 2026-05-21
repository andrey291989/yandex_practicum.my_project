package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        // Given
        String username = "testuser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String role = "USER";

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setPassword(encodedPassword);
        savedUser.setRole(role);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // When
        Mono<User> result = userService.createUser(username, rawPassword, role);

        // Then
        User user = result.block();
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(encodedPassword, user.getPassword());
        assertEquals(role, user.getRole());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testFindByUsername_UserExists() {
        // Given
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole("USER");
        user.setEnabled(true);

        when(userRepository.findByUsernameAndEnabledTrue(username)).thenReturn(Mono.just(user));

        // When
        Mono<org.springframework.security.core.userdetails.UserDetails> result =
            userService.findByUsername(username);

        // Then
        org.springframework.security.core.userdetails.UserDetails userDetails = result.block();
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertTrue(userDetails.isEnabled());

        verify(userRepository).findByUsernameAndEnabledTrue(username);
    }

    @Test
    void testFindByUsername_UserNotFound() {
        // Given
        String username = "nonexistent";

        when(userRepository.findByUsernameAndEnabledTrue(username)).thenReturn(Mono.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.findByUsername(username).block();
        });

        verify(userRepository).findByUsernameAndEnabledTrue(username);
    }
}