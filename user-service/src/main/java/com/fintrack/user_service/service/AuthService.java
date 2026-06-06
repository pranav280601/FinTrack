package com.fintrack.user_service.service;

import com.fintrack.user_service.dto.AuthResponse;
import com.fintrack.user_service.dto.LoginRequest;
import com.fintrack.user_service.dto.RegisterRequest;
import com.fintrack.user_service.exception.InvalidCredentialsException;
import com.fintrack.user_service.exception.UserAlreadyExistsException;
import com.fintrack.user_service.model.User;
import com.fintrack.user_service.repository.UserRepository;
import com.fintrack.user_service.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new InvalidCredentialsException());

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException();
        }
        String token = jwtUtil.generateToken(user.getId().toString(),user.getEmail(), user.getRole().name());
        return new AuthResponse(token,user.getId(),user.getEmail(), user.getRole().name());
    }
}