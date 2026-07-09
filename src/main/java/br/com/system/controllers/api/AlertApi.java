package br.com.system.controllers.api;

import br.com.system.data.dto.request.AlertRequestDTO;
import br.com.system.data.dto.response.AlertResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Alert", description = "Endpoints for alert management.")
public interface AlertApi {

    @Operation(summary = "List alerts", description = "Returns all alerts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AlertResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<AlertResponseDTO> findAll();

    @Operation(summary = "List active alerts", description = "Returns all active alerts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AlertResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<AlertResponseDTO> findActive();

    @Operation(summary = "List unread alerts", description = "Returns all unread alerts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AlertResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<AlertResponseDTO> findUnread();

    @Operation(summary = "List alerts by administrator", description = "Returns alerts linked to the informed administrator.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AlertResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Administrator not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<AlertResponseDTO> findByAdmin(@PathVariable("adminId") Long adminId);

    @Operation(summary = "List alerts by product", description = "Returns alerts linked to the informed product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AlertResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<AlertResponseDTO> findByProduct(@PathVariable("productId") Long productId);

    @Operation(summary = "Get alert by id", description = "Returns the alert for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlertResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    AlertResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "Create alert", description = "Creates a new alert.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alert created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlertResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<AlertResponseDTO> create(@RequestBody AlertRequestDTO alert);

    @Operation(summary = "Update alert", description = "Updates the alert data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlertResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    AlertResponseDTO update(@PathVariable("id") Long id, @RequestBody AlertRequestDTO alert);

    @Operation(summary = "Mark alert as read", description = "Marks the alert as read.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlertResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    AlertResponseDTO markAsRead(@PathVariable("id") Long id);

    @Operation(summary = "Delete alert", description = "Deletes the alert.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alert deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long id);
}
