package com.promptwars.task.dto;

import com.promptwars.task.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class TaskDtos {

    private TaskDtos() {}

    public record CreateTaskRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 200)
            String title,

            String description,

            @NotNull(message = "boardId is required")
            UUID boardId,

            UUID teamId,
            UUID assigneeId,
            Task.Priority priority,
            Instant dueAt
    ) {}

    public record UpdateTaskRequest(
            @Size(max = 200)
            String title,

            String description,
            Task.TaskStatus status,
            Task.Priority priority,
            UUID assigneeId,
            Instant dueAt,
            Integer positionOrder
    ) {}

    public record MoveTaskRequest(
            @NotNull
            Task.TaskStatus targetStatus,

            @NotNull
            Integer targetPosition
    ) {}

    public record TaskResponse(
            UUID id,
            String title,
            String description,
            Task.TaskStatus status,
            Task.Priority priority,
            UUID boardId,
            UUID teamId,
            UUID assigneeId,
            UUID reporterId,
            Instant dueAt,
            Integer positionOrder,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record BoardSummary(
            UUID boardId,
            long todo,
            long inProgress,
            long inReview,
            long done,
            long blocked
    ) {}
}
