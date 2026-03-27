package com.shubhasamagri.service;

import com.shubhasamagri.dto.request.LoginRequest;
import com.shubhasamagri.dto.request.SignupRequest;
import com.shubhasamagri.dto.response.AuthResponse;
import com.shubhasamagri.entity.User;
import com.shubhasamagri.exception.BadRequestException;
import com.shubhasamagri.repository.UserRepository;
import com.shubhasamagri.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user authentication and registration.
 *
 * JWT Flow:
 *   1. User submits email/password
 *   2. AuthenticationManager validates credentials against DB
 *   3. JwtTokenProvider generates signed JWT
 *   4. Token returned to client (stored in localStorage)
 *   5. Subsequent requests send token in Authorization header
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     * Validates email uniqueness, encodes password, saves user, and returns JWT.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered. Please login.");
        }

        // Create and save the new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getName(), user.getEmail());

        // Generate JWT token
        String token = generateTokenForUser(user);

        return buildAuthResponse(token, user);
    }

    /**
     * Authenticate an existing user.
     * Throws BadCredentialsException (401) if credentials are invalid.
     */
    public AuthResponse login(LoginRequest request) {
        // Spring Security validates credentials against DB
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtTokenProvider.generateToken(userDetails);
        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(token, user);
    }

    private String generateTokenForUser(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
        return jwtTokenProvider.generateToken(userDetails);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
