package com.micro.demo1.demo1;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserKafkaListener {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_RESPONSES_TOPIC = "user-responses";

    @KafkaListener(topics = "user-requests", groupId = "user-service-group")
    public void listenUserRequests(String message) throws Exception {
        Map<String, String> request = objectMapper.readValue(message, new TypeReference<>() {});
        String requestId = request.get("requestId");
        String username = request.get("username");

        Long userId = userRepository.findByUsername(username)
                                    .map(user -> user.getId())
                                    .orElse(null);

        Map<String, Object> response = Map.of(
            "requestId", requestId,
            "userId", userId
        );

        String responseMessage = objectMapper.writeValueAsString(response);

        kafkaTemplate.send(USER_RESPONSES_TOPIC, responseMessage);
    }
}
