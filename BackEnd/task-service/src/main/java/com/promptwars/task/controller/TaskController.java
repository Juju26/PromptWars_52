package com.promptwars.task.controller;

import com.promptwars.common.dto.ApiResponse;
import com.promptwars.task.dto.TaskDtos;
import com.promptwars.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Kanban task management")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task")
    public ApiResponse<TaskDtos.TaskResponse> createTask(
            @Valid @RequestBody TaskDtos.CreateTaskRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ApiResponse.ok("Task created", taskService.createTask(request, UUID.fromString(userId)));
    }

    @GetMapping("/board/{boardId}")
    @Operation(summary = "Get all tasks for a Kanban board (ordered by status + position)")
    public ApiResponse<List<TaskDtos.TaskResponse>> getBoardTasks(@PathVariable UUID boardId) {
        return ApiResponse.ok(taskService.getBoardTasks(boardId));
    }

    @GetMapping("/board/{boardId}/summary")
    @Operation(summary = "Get task count summary per status for a board")
    public ApiResponse<TaskDtos.BoardSummary> getBoardSummary(@PathVariable UUID boardId) {
        return ApiResponse.ok(taskService.getBoardSummary(boardId));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get a single task by ID")
    public ApiResponse<TaskDtos.TaskResponse> getTask(@PathVariable UUID taskId) {
        return ApiResponse.ok(taskService.getTask(taskId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get tasks assigned to the authenticated user")
    public ApiResponse<Page<TaskDtos.TaskResponse>> getMyTasks(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable
    ) {
        return ApiResponse.ok(taskService.getMyTasks(UUID.fromString(userId), pageable));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update task details (title, description, priority, assignee)")
    public ApiResponse<TaskDtos.TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskDtos.UpdateTaskRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ApiResponse.ok("Task updated", taskService.updateTask(taskId, request, UUID.fromString(userId)));
    }

    @PatchMapping("/{taskId}/move")
    @Operation(summary = "Move task to a different Kanban column / position")
    public ApiResponse<TaskDtos.TaskResponse> moveTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskDtos.MoveTaskRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ApiResponse.ok("Task moved", taskService.moveTask(taskId, request, UUID.fromString(userId)));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    public void deleteTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal String userId
    ) {
        taskService.deleteTask(taskId, UUID.fromString(userId));
    }
}
