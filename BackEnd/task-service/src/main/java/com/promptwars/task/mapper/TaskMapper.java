package com.promptwars.task.mapper;

import com.promptwars.task.domain.Task;
import com.promptwars.task.dto.TaskDtos;
import org.mapstruct.*;

/**
 * MapStruct mapper — null-safe partial updates via IGNORE strategy.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    TaskDtos.TaskResponse toResponse(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "TODO")
    Task toEntity(TaskDtos.CreateTaskRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardId", ignore = true)
    @Mapping(target = "reporterId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Task task, TaskDtos.UpdateTaskRequest request);
}
