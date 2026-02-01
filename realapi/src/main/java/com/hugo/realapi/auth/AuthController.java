package com.hugo.realapi.auth;

import com.hugo.realapi.auth.dto.AuthDtos;
import com.hugo.realapi.security.JwtUtil;
import com.hugo.realapi.user.User;
import com.hugo.realapi.user.UserRepo;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepo users;
    private final JwtUtil jwt;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepo users, JwtUtil jwt, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.jwt = jwt;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        String email = req.email().toLowerCase();

        if (users.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u = users.save(u);

        String token = jwt.createToken(u.getId(), u.getEmail());
        return new AuthDtos.AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        String email = req.email().toLowerCase();

        User u = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bad credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("Bad credentials");
        }

        String token = jwt.createToken(u.getId(), u.getEmail());
        return new AuthDtos.AuthResponse(token);
    }
}

