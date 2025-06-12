package com.micro.dem2.demo2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Task>> getTasksByUser(@RequestHeader("X-Username") String username) {
        List<Task> tasks = taskService.getTasksByUsername(username);
        return ResponseEntity.ok(tasks);
    }

    // Now requires X-Username header
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, @RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(taskService.createTask(task, username));
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

