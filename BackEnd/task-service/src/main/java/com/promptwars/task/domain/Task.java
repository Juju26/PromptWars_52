package com.promptwars.task.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Task entity representing a Kanban card.
 * assigneeId and reporterId reference user UUIDs in user-service (no FK — microservice boundary).
 */
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_board_id", columnList = "board_id"),
        @Index(name = "idx_tasks_assignee", columnList = "assignee_id"),
        @Index(name = "idx_tasks_status", columnList = "status"),
        @Index(name = "idx_tasks_team_id", columnList = "team_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "position_order")
    @Builder.Default
    private Integer positionOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum TaskStatus {
        TODO, IN_PROGRESS, IN_REVIEW, DONE, BLOCKED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
