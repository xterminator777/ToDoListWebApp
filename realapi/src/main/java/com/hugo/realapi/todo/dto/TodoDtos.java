package com.hugo.realapi.todo.dto;

import jakarta.validation.constraints.NotBlank;

public class TodoDtos {
    public record CreateTodoRequest(@NotBlank String title) {}
}

