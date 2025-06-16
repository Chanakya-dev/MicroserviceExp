package com.micro.dem2.demo2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
    @GetMapping("/user-tasks")
    public ResponseEntity<?> getTasksByUser(@RequestHeader("X-Username") String username) {
        try {
            List<Task> tasks = taskService.getTasksByUsername(username);
            if (tasks.isEmpty()) {
                // When fallback returns empty list, you can respond with a message or status
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Rate limit exceeded or fallback triggered. Please try again later.");
            }
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            // This should not normally happen because circuit breaker fallback will handle
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error occurred.");
        }
    }


    // Now requires X-Username header
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task, @RequestHeader("X-Username") String username) {
        try {
            Task createdTask = taskService.createTask(task, username);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            // Log the error if needed
            System.err.println("Error creating task: " + e.getMessage());

            // Return an appropriate error response
            return ResponseEntity.status(503).body("Service unavailable. Please try again later.");
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value="/notify", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> receiveNotification(@RequestBody String message) {
        System.out.println("Notification received: " + message);
        return ResponseEntity.ok("Notification received");
    }
}

