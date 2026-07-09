package br.com.system.controllers.api;

import br.com.system.data.dto.request.SupplierRequestDTO;
import br.com.system.data.dto.response.SupplierResponseDTO;
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

@Tag(name = "Supplier", description = "Endpoints for supplier management.")
public interface SupplierApi {
    @Operation(summary = "Create supplier", description = "Creates a new supplier.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<SupplierResponseDTO> create(@RequestBody SupplierRequestDTO dto);

    @Operation(summary = "Update supplier", description = "Updates the supplier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<SupplierResponseDTO> update(@PathVariable("id") Long id, @RequestBody SupplierRequestDTO dto);

    @Operation(summary = "Toggle supplier active flag", description = "Toggles the active flag for the supplier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supplier updated successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<Void> toggleActive(@PathVariable("id") Long id);

    @Operation(summary = "Get supplier by id", description = "Returns the supplier for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<SupplierResponseDTO> findById(@PathVariable("id") Long id);

    @Operation(summary = "List suppliers", description = "Returns all suppliers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suppliers found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SupplierResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<SupplierResponseDTO>> findAll();

    @Operation(summary = "List active suppliers", description = "Returns all active suppliers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suppliers found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SupplierResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<SupplierResponseDTO>> findAllActive();

    @Operation(summary = "List disabled suppliers", description = "Returns all disabled suppliers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suppliers found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SupplierResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<SupplierResponseDTO>> findAllDisabled();
}
