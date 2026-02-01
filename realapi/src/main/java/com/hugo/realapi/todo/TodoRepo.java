package com.hugo.realapi.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TodoRepo extends JpaRepository<Todo, Long> {
    List<Todo> findByUserIdOrderByIdDesc(Long userId);
    Optional<Todo> findByIdAndUserId(Long id, Long userId);
}

