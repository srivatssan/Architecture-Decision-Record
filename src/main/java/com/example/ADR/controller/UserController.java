package com.example.ADR.controller;

import com.example.ADR.model.User;
import com.example.ADR.repository.UserRepository;
import com.example.ADR.util.AesEncryptor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered.");
        }
        return ResponseEntity.ok(userRepo.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String encryptedPassword = loginRequest.get("password");
        System.out.println("Email: "+email);
        System.out.println("password: "+encryptedPassword);
        Optional<User> optionalUser = userRepo.findByEmail(email.trim());
        System.out.println("User found? " + optionalUser.isPresent());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String encryptedStored = AesEncryptor.encrypt(user.getPassword());
            System.out.println("Password stored in DB: "+encryptedStored);

            if (encryptedStored.equals(encryptedPassword)) {
                Map<String, String> response = new HashMap<>();
                response.put("email", user.getEmail());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("role", user.getRole());
                return ResponseEntity.ok(response);
            }
        }
        System.out.println("user not present");
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
