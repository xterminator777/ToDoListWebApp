package com.hugo.realapi.todo;

import com.hugo.realapi.todo.dto.TodoDtos;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoRepo repo;

    public TodoController(TodoRepo repo) {
        this.repo = repo;
    }

    private Long userId(Authentication auth) {
        Object p = auth.getPrincipal();
        if (p instanceof Long id) return id;
        // In case Spring represents it differently:
        return Long.parseLong(p.toString());
    }

    @GetMapping
    public List<Todo> list(Authentication auth) {
        return repo.findByUserIdOrderByIdDesc(userId(auth));
    }

    @PostMapping
    public Todo create(Authentication auth, @Valid @RequestBody TodoDtos.CreateTodoRequest req) {
        Todo t = new Todo();
        t.setUserId(userId(auth));
        t.setTitle(req.title());
        t.setDone(false);
        return repo.save(t);
    }

    @PatchMapping("/{id}/toggle")
    public Todo toggle(Authentication auth, @PathVariable Long id) {
        Todo t = repo.findByIdAndUserId(id, userId(auth)).orElseThrow();
        t.setDone(!t.isDone());
        return repo.save(t);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable Long id) {
        Todo t = repo.findByIdAndUserId(id, userId(auth)).orElseThrow();
        repo.delete(t);
    }
}

