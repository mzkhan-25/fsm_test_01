package com.fsm.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single address suggestion with coordinates.
 * Used as response item from address autocomplete endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Address suggestion with formatted address and coordinates")
public class AddressSuggestionResponse {
    
    @Schema(description = "Formatted address string", example = "123 Main St, Springfield, IL 62701, USA")
    private String formattedAddress;
    
    @Schema(description = "Latitude coordinate", example = "39.7817")
    private Double latitude;
    
    @Schema(description = "Longitude coordinate", example = "-89.6501")
    private Double longitude;
    
    @Schema(description = "Place ID from the mapping provider", example = "ChIJd8BlQ2BZwokRAFUEcm_qrcA")
    private String placeId;
}
