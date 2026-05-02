package com.promptwars.task.service;

import com.promptwars.common.exception.BusinessException;
import com.promptwars.common.exception.ResourceNotFoundException;
import com.promptwars.task.domain.Task;
import com.promptwars.task.dto.TaskDtos;
import com.promptwars.task.mapper.TaskMapper;
import com.promptwars.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskDtos.TaskResponse createTask(TaskDtos.CreateTaskRequest request, UUID reporterId) {
        Task task = taskMapper.toEntity(request);
        task.setReporterId(reporterId);
        task = taskRepository.save(task);
        log.info("Task created: id={} by user={}", task.getId(), reporterId);
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDtos.TaskResponse> getBoardTasks(UUID boardId) {
        return taskRepository.findByBoardIdOrdered(boardId)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDtos.TaskResponse getTask(UUID taskId) {
        return taskMapper.toResponse(findOrThrow(taskId));
    }

    @Transactional(readOnly = true)
    public Page<TaskDtos.TaskResponse> getMyTasks(UUID assigneeId, Pageable pageable) {
        return taskRepository.findByAssigneeId(assigneeId, pageable)
                .map(taskMapper::toResponse);
    }

    @Transactional
    public TaskDtos.TaskResponse updateTask(UUID taskId, TaskDtos.UpdateTaskRequest request, UUID requesterId) {
        Task task = findOrThrow(taskId);
        validateOwnerOrAssignee(task, requesterId);
        taskMapper.updateEntity(task, request);
        task = taskRepository.save(task);
        log.info("Task updated: id={} by user={}", taskId, requesterId);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskDtos.TaskResponse moveTask(UUID taskId, TaskDtos.MoveTaskRequest request, UUID requesterId) {
        Task task = findOrThrow(taskId);
        task.setStatus(request.targetStatus());
        task.setPositionOrder(request.targetPosition());
        task = taskRepository.save(task);
        log.info("Task moved: id={} -> status={} pos={}", taskId, request.targetStatus(), request.targetPosition());
        return taskMapper.toResponse(task);
    }

    @Transactional
    public void deleteTask(UUID taskId, UUID requesterId) {
        Task task = findOrThrow(taskId);
        validateOwnerOrAssignee(task, requesterId);
        taskRepository.delete(task);
        log.info("Task deleted: id={} by user={}", taskId, requesterId);
    }

    @Transactional(readOnly = true)
    public TaskDtos.BoardSummary getBoardSummary(UUID boardId) {
        return new TaskDtos.BoardSummary(
                boardId,
                taskRepository.countByBoardIdAndStatus(boardId, Task.TaskStatus.TODO),
                taskRepository.countByBoardIdAndStatus(boardId, Task.TaskStatus.IN_PROGRESS),
                taskRepository.countByBoardIdAndStatus(boardId, Task.TaskStatus.IN_REVIEW),
                taskRepository.countByBoardIdAndStatus(boardId, Task.TaskStatus.DONE),
                taskRepository.countByBoardIdAndStatus(boardId, Task.TaskStatus.BLOCKED)
        );
    }

    private Task findOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private void validateOwnerOrAssignee(Task task, UUID requesterId) {
        boolean isReporter = task.getReporterId().equals(requesterId);
        boolean isAssignee = requesterId.equals(task.getAssigneeId());
        if (!isReporter && !isAssignee) {
            throw new BusinessException("You do not have permission to modify this task");
        }
    }
}
