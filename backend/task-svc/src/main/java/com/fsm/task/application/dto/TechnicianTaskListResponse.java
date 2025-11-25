package com.fsm.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for technician's task list response.
 * Contains a list of tasks assigned to the technician.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing a list of technician's assigned tasks")
public class TechnicianTaskListResponse {
    
    @Schema(description = "List of tasks assigned to the technician")
    private List<TechnicianTaskResponse> tasks;
    
    @Schema(description = "Total number of tasks matching the filter", example = "5")
    private int totalTasks;
}
