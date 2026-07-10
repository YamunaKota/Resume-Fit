package com.resumefit.resumefit.contoller;



import com.resumefit.resumefit.dto.*;
import com.resumefit.resumefit.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public String signup(
            @RequestBody RegisterRequest request
    ) {

        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody AuthRequest request
    ) {

        return authService.login(request);
    }

    @GetMapping("/me")
    public UserProfileResponse me(
            @RequestHeader("Authorization") String authorization
    ) {
        return authService.me(authorization);
    }
}
