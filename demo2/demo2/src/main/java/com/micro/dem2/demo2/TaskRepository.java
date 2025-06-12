package com.micro.dem2.demo2;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserId(Long userId);
}
