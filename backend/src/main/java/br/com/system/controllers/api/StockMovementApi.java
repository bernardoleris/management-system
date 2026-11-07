package br.com.system.controllers.api;

import br.com.system.data.dto.request.StockMovementRequestDTO;
import br.com.system.data.dto.response.StockMovementResponseDTO;
import br.com.system.enums.MovementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Stock Movement", description = "Endpoints for stock movement management.")
public interface StockMovementApi {
    @Operation(summary = "List stock movements", description = "Returns all stock movements paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "date,desc")
    })
    Page<StockMovementResponseDTO> findAll(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "date") Pageable pageable);

    @Operation(summary = "Get stock movement by id", description = "Returns the stock movement for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movement found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockMovementResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Stock movement not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    StockMovementResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "List stock movements by administrator", description = "Returns stock movements linked to the informed administrator paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Administrator not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "date,desc")
    })
    Page<StockMovementResponseDTO> findByAdmin(
            @PathVariable("adminId") Long adminId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "date") Pageable pageable);

    @Operation(summary = "List stock movements by supplier", description = "Returns stock movements linked to the informed supplier paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "date,desc")
    })
    Page<StockMovementResponseDTO> findBySupplier(
            @PathVariable("supplierId") Long supplierId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "date") Pageable pageable);

    @Operation(summary = "List stock movements by type", description = "Returns stock movements filtered by type paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "date,desc")
    })
    Page<StockMovementResponseDTO> findByType(
            @PathVariable("type") MovementType type,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "date") Pageable pageable);

    @Operation(summary = "Create stock movement", description = "Creates a new stock movement.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock movement created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockMovementResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<StockMovementResponseDTO> create(@RequestBody StockMovementRequestDTO dto);

    @Operation(summary = "Delete stock movement", description = "Deletes the stock movement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Stock movement deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Stock movement not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<Void> delete(@PathVariable("id") Long id);
}
