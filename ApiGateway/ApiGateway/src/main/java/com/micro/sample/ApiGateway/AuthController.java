package com.micro.sample.ApiGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    private final String USER_SERVICE_URL = "http://localhost:8081/api/cvc/validate";


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        RestTemplate restTemplate = new RestTemplate();

        // Create HttpHeaders and set content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with body and headers
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(credentials, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    USER_SERVICE_URL,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String token = jwtUtil.generateToken(credentials.get("username"));
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Auth failed: " + e.getMessage());
        }
    }

}
