package com.cagla.ecologix.service;

import com.cagla.ecologix.model.User;
import com.cagla.ecologix.model.enums.UserRole;
import com.cagla.ecologix.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.cagla.ecologix.model.BranchInfo;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    public Map<String, Object> register(Map<String, Object> request) {
        String username = String.valueOf(request.get("username"));
        String password = String.valueOf(request.get("password"));
        String role = String.valueOf(request.get("role"));

        if (username == null || username.isBlank() || username.equals("null")) {
            throw new RuntimeException("Kullanıcı adı zorunludur.");
        }

        if (password == null || password.isBlank() || password.equals("null")) {
            throw new RuntimeException("Şifre zorunludur.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(UserRole.valueOf(role));

        Object courierIdValue = request.get("courierId");
        if (courierIdValue != null) {
            user.setCourierId(Long.valueOf(String.valueOf(courierIdValue)));
        }

        BranchInfo branchInfo = new BranchInfo();
        branchInfo.setBranchName(String.valueOf(request.get("branchName")));
        branchInfo.setBranchCity(String.valueOf(request.get("branchCity")));
        branchInfo.setBranchAddress(String.valueOf(request.get("branchAddress")));

        if (request.get("branchLat") != null) {
            branchInfo.setBranchLat(Double.valueOf(String.valueOf(request.get("branchLat"))));
        }

        if (request.get("branchLon") != null) {
            branchInfo.setBranchLon(Double.valueOf(String.valueOf(request.get("branchLon"))));
        }

        user.setBranchInfo(branchInfo);

        User savedUser = userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", savedUser.getId());
        result.put("username", savedUser.getUsername());
        result.put("role", savedUser.getRole().toString());
        result.put("courierId", savedUser.getCourierId());

        if (savedUser.getBranchInfo() != null) {
            result.put("branchName", savedUser.getBranchInfo().getBranchName());
            result.put("branchCity", savedUser.getBranchInfo().getBranchCity());
            result.put("branchAddress", savedUser.getBranchInfo().getBranchAddress());
            result.put("branchLat", savedUser.getBranchInfo().getBranchLat());
            result.put("branchLon", savedUser.getBranchInfo().getBranchLon());
        }

        return result;
    }

    public Map<String, Object> login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Kullanıcı adı zorunludur.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Şifre zorunludur.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı adı veya şifre hatalı."));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Kullanıcı adı veya şifre hatalı.");
        }

        Map<String, Object> sessionData = new HashMap<>();

        sessionData.put("id", user.getId());
        sessionData.put("username", user.getUsername());
        sessionData.put("role", user.getRole().toString());
        sessionData.put("courierId", user.getCourierId());

        if (user.getBranchInfo() != null) {
            sessionData.put("branchName", user.getBranchInfo().getBranchName());
            sessionData.put("branchCity", user.getBranchInfo().getBranchCity());
            sessionData.put("branchAddress", user.getBranchInfo().getBranchAddress());
            sessionData.put("branchLat", user.getBranchInfo().getBranchLat());
            sessionData.put("branchLon", user.getBranchInfo().getBranchLon());
        }

        return sessionData;
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Kullanıcı adı zorunludur.");
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            throw new RuntimeException("Mevcut şifre zorunludur.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Yeni şifre zorunludur.");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Yeni şifre en az 6 karakter olmalıdır.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Mevcut şifre hatalı.");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}