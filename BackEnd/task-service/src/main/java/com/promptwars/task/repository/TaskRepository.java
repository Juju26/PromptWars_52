package com.promptwars.task.repository;

import com.promptwars.task.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Fetch all tasks for a board ordered by column status then position — optimised for Kanban render.
     */
    @Query("SELECT t FROM Task t WHERE t.boardId = :boardId ORDER BY t.status, t.positionOrder ASC")
    List<Task> findByBoardIdOrdered(UUID boardId);

    /**
     * Tasks assigned to a user — paginated for "My Tasks" view.
     */
    Page<Task> findByAssigneeId(UUID assigneeId, Pageable pageable);

    /**
     * Team-scoped task listing for leaderboard aggregation.
     */
    Page<Task> findByTeamId(UUID teamId, Pageable pageable);

    /**
     * Status + team filter — used for progress reporting.
     */
    @Query("SELECT t FROM Task t WHERE t.teamId = :teamId AND t.status = :status ORDER BY t.updatedAt DESC")
    List<Task> findByTeamIdAndStatus(UUID teamId, Task.TaskStatus status);

    long countByBoardIdAndStatus(UUID boardId, Task.TaskStatus status);
}
