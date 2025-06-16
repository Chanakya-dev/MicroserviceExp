package com.micro.dem2.demo2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_REQUESTS_TOPIC = "user-requests";
    private static final String USER_RESPONSES_TOPIC = "user-responses";

    private final ConcurrentHashMap<String, CompletableFuture<Long>> responseMap = new ConcurrentHashMap<>();

    @CircuitBreaker(name = "taskServiceCB", fallbackMethod = "getTasksByUsernameFallback")
    public List<Task> getTasksByUsername(String username) throws Exception {
        String redisKey = "tasks::" + username;

        List<Task> cachedTasks = null;
        try {
            cachedTasks = (List<Task>) redisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            System.err.println("Redis access failed, continuing without cache: " + e.getMessage());
        }

        if (cachedTasks != null) {
            System.out.println("Returning tasks from Redis cache for user: " + username);
            return cachedTasks;
        }

        // Cache miss or Redis down - fetch userId via Kafka flow
        String requestId = UUID.randomUUID().toString();
        Map<String, String> payload = Map.of("requestId", requestId, "username", username);
        String message = objectMapper.writeValueAsString(payload);

        CompletableFuture<Long> future = new CompletableFuture<>();
        responseMap.put(requestId, future);

        kafkaTemplate.send(USER_REQUESTS_TOPIC, message);

        Long userId = future.get(5, TimeUnit.SECONDS);

        List<Task> tasks = taskRepository.findByUserId(userId);

        // Save to Redis cache with TTL (10 minutes)
        try {
            redisTemplate.opsForValue().set(redisKey, tasks, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Failed to write to Redis cache: " + e.getMessage());
        }

        return tasks;
    }


    public List<Task> getTasksByUsernameFallback(String username, Throwable throwable) {
        System.err.println("Fallback triggered for getTasksByUsername due to: " + throwable.getMessage());
        return taskRepository.findAll();
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
        String redisKey = "tasks::all";

        List<Task> cachedTasks = (List<Task>) redisTemplate.opsForValue().get(redisKey);
        if (cachedTasks != null) {
            System.out.println("Returning all tasks from Redis cache");
            return cachedTasks;
        }

        List<Task> tasks = taskRepository.findAll();
        redisTemplate.opsForValue().set(redisKey, tasks, 10, TimeUnit.MINUTES);
        return tasks;
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

            Task savedTask = taskRepository.save(task);

            // Evict related Redis cache keys
            redisTemplate.delete("tasks::" + username);
            redisTemplate.delete("tasks::all");

            return savedTask;
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign userId during task creation", e);
        }
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task existing = taskRepository.findById(id).orElseThrow();
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        Task savedTask = taskRepository.save(existing);

        // Evict all caches related to tasks - you can improve by evicting only relevant keys if you track user
        redisTemplate.delete("tasks::all");
        // If you track username or userId, evict user cache as well here

        return savedTask;
    }

    public void deleteTask(Long id) {
        // Before deleting, find the task to get user info for cache eviction
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            taskRepository.deleteById(id);
            redisTemplate.delete("tasks::all");
            redisTemplate.delete("tasks::" + task.getUserId()); // assuming userId is String, convert if needed
        }
    }
}
