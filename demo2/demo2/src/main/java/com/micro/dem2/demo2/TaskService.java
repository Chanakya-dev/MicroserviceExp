package com.micro.dem2.demo2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_REQUESTS_TOPIC = "user-requests";
    private static final String USER_RESPONSES_TOPIC = "user-responses";

    private final ConcurrentHashMap<String, CompletableFuture<Long>> responseMap = new ConcurrentHashMap<>();

    public List<Task> getTasksByUsername(String username) {
        try {
            String requestId = UUID.randomUUID().toString();

            Map<String, String> payload = Map.of("requestId", requestId, "username", username);
            String message = objectMapper.writeValueAsString(payload);

            CompletableFuture<Long> future = new CompletableFuture<>();
            responseMap.put(requestId, future);

            kafkaTemplate.send(USER_REQUESTS_TOPIC, message);

            Long userId = future.get(5, TimeUnit.SECONDS);

            return taskRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get userId from UserService", e);
        }
    }

    @KafkaListener(topics = USER_RESPONSES_TOPIC, groupId = "task-service-group")
    public void listenUserResponses(String message) throws Exception {
        Map<String, Object> response = objectMapper.readValue(message, new TypeReference<>() {});
        String requestId = (String) response.get("requestId");
        Long userId = Long.valueOf(response.get("userId").toString());

        CompletableFuture<Long> future = responseMap.remove(requestId);
        if (future != null) {
            future.complete(userId);
        }
    }

    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task, String username) {
        try {
            String requestId = UUID.randomUUID().toString();
            Map<String, String> payload = Map.of("requestId", requestId, "username", username);
            String message = objectMapper.writeValueAsString(payload);

            CompletableFuture<Long> future = new CompletableFuture<>();
            responseMap.put(requestId, future);
            kafkaTemplate.send(USER_REQUESTS_TOPIC, message);

            Long userId = future.get(5, TimeUnit.SECONDS);

            task.setUserId(userId);

            return taskRepository.save(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign userId during task creation", e);
        }
    }


    public Task updateTask(Long id, Task updatedTask) {
        Task existing = taskRepository.findById(id).orElseThrow();
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        return taskRepository.save(existing);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

}
