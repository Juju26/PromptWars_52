package com.promptwars.task.service;

import com.promptwars.common.exception.BusinessException;
import com.promptwars.common.exception.ResourceNotFoundException;
import com.promptwars.task.domain.Task;
import com.promptwars.task.dto.TaskDtos;
import com.promptwars.task.mapper.TaskMapper;
import com.promptwars.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private UUID taskId;
    private UUID requesterId;
    private Task task;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .reporterId(requesterId)
                .assigneeId(requesterId)
                .build();
    }

    @Test
    void createTask_Success() {
        TaskDtos.CreateTaskRequest request = new TaskDtos.CreateTaskRequest(
                "Test Task", "Desc", UUID.randomUUID(), null, null, null, null);
        
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(new TaskDtos.TaskResponse(
                taskId, "Test Task", "Desc", Task.TaskStatus.TODO, Task.Priority.MEDIUM,
                request.boardId(), null, requesterId, requesterId, null, 0, null, null
        ));

        TaskDtos.TaskResponse response = taskService.createTask(request, requesterId);

        assertNotNull(response);
        assertEquals("Test Task", response.title());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTask_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(new TaskDtos.TaskResponse(
                taskId, "Test Task", null, Task.TaskStatus.TODO, Task.Priority.MEDIUM,
                null, null, requesterId, requesterId, null, 0, null, null
        ));

        TaskDtos.TaskResponse response = taskService.getTask(taskId);

        assertNotNull(response);
        assertEquals(taskId, response.id());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTask_NotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTask(taskId));
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> taskService.deleteTask(taskId, requesterId));

        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_NoPermission() {
        UUID otherUserId = UUID.randomUUID();
        task.setReporterId(otherUserId);
        task.setAssigneeId(otherUserId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> taskService.deleteTask(taskId, requesterId));

        verify(taskRepository, never()).delete(any());
    }
}
