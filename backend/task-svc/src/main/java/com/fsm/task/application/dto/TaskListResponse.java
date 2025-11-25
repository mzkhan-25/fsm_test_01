package com.fsm.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for task list response with pagination and status counts.
 * Contains task list, pagination info, and metadata with status counts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for task list with pagination and metadata")
public class TaskListResponse {
    
    @Schema(description = "List of tasks matching the query")
    private List<TaskResponse> tasks;
    
    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;
    
    @Schema(description = "Number of items per page", example = "50")
    private int pageSize;
    
    @Schema(description = "Total number of items matching the query", example = "100")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "2")
    private int totalPages;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
    
    @Schema(description = "Task count by status (UNASSIGNED, ASSIGNED, IN_PROGRESS, COMPLETED)")
    private Map<String, Long> statusCounts;
}
