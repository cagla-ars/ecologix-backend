package com.cagla.ecologix.controller;

import com.cagla.ecologix.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> sessionData = authService.login(username, password);

        return ResponseEntity.ok(sessionData);
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        Map<String, Object> createdUser = authService.register(request);

        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        authService.changePassword(username, oldPassword, newPassword);

        return ResponseEntity.ok("Şifre başarıyla güncellendi.");
    }
}