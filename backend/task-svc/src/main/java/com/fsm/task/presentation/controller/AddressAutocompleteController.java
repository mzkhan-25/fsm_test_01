package com.fsm.task.presentation.controller;

import com.fsm.task.application.dto.AddressSuggestionResponse;
import com.fsm.task.application.service.AddressAutocompleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for address autocomplete functionality.
 * Provides endpoint for getting address suggestions based on partial input.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Address Autocomplete", description = "Address autocomplete API for task creation")
public class AddressAutocompleteController {
    
    private final AddressAutocompleteService addressAutocompleteService;
    
    /**
     * Gets address suggestions for a partial address input.
     * Results include formatted addresses with coordinates for map display.
     * Rate limiting is applied to prevent API quota exhaustion.
     * Results are cached for frequently queried addresses.
     * 
     * @param partialAddress the partial address to search for (minimum 3 characters)
     * @return list of address suggestions with coordinates
     */
    @GetMapping("/address-suggestions")
    @Operation(
            summary = "Get address suggestions",
            description = "Returns address autocomplete suggestions with coordinates based on partial address input. " +
                    "Results are cached and rate-limited to prevent API quota exhaustion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Address suggestions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressSuggestionResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - partial address too short",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded - too many requests",
                    content = @Content
            )
    })
    public ResponseEntity<List<AddressSuggestionResponse>> getAddressSuggestions(
            @Parameter(description = "Partial address to search for", example = "123 Main St", required = true)
            @RequestParam
            @NotBlank(message = "Partial address is required")
            @Size(min = 3, message = "Partial address must be at least 3 characters")
            String partialAddress) {
        
        log.info("Received address suggestions request for: {}", partialAddress);
        
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        log.info("Returning {} address suggestions", suggestions.size());
        return ResponseEntity.ok(suggestions);
    }
}
