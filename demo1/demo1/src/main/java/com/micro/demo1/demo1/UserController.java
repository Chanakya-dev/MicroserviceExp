package com.micro.demo1.demo1;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cvc")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken.");
        }
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }
    @PostMapping("/validate")
    public ResponseEntity<String> validateUser(@RequestBody User user) {
        Optional<User> found = userRepository.findAll().stream()
            .filter(u -> u.getUsername().equals(user.getUsername()) && u.getPassword().equals(user.getPassword()))
            .findFirst();

        if (found.isPresent()) {
            return ResponseEntity.ok("User valid");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
    }

}
