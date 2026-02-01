package com.hugo.realapi.todo;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "todos")
public class Todo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false)
    private boolean done = false;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setDone(boolean done) { this.done = done; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

