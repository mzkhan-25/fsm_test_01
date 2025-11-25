package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task list query parameters.
 * Supports filtering by status, priority, search, and pagination/sorting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request parameters for querying task list")
public class TaskListRequest {
    
    @Schema(description = "Filter by task status", example = "UNASSIGNED")
    private TaskStatus status;
    
    @Schema(description = "Filter by task priority", example = "HIGH")
    private Priority priority;
    
    @Schema(description = "Search across title, id, and client address (case-insensitive)", example = "HVAC")
    private String search;
    
    @Schema(description = "Field to sort by: priority, createdAt, status", example = "priority", allowableValues = {"priority", "createdAt", "status"})
    @Builder.Default
    private String sortBy = "priority";
    
    @Schema(description = "Sort order: asc or desc", example = "desc", allowableValues = {"asc", "desc"})
    @Builder.Default
    private String sortOrder = "desc";
    
    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based)", example = "0", minimum = "0")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Number of items per page (default 50, max 100)", example = "50", minimum = "1", maximum = "100")
    @Builder.Default
    private Integer pageSize = 50;
}
