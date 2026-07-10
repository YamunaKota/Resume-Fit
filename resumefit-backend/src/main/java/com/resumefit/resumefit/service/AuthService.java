package com.resumefit.resumefit.service;



import com.resumefit.resumefit.dto.*;
import com.resumefit.resumefit.entity.User;
import com.resumefit.resumefit.repository.UserRepository;
import com.resumefit.resumefit.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String signup(RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
        System.out.println(user.getEmail());
        System.out.println("saved");

        return "User registered successfully";
    }

    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow();

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            token
        );
    }

    public UserProfileResponse me(String token) {
        String rawToken = token.replace("Bearer ", "").trim();
        String email = jwtUtil.extractEmail(rawToken);

        User user = userRepository.findByEmail(email).orElseThrow();

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
