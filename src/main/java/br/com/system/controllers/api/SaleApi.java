package br.com.system.controllers.api;

import br.com.system.data.dto.request.SaleRequestDTO;
import br.com.system.data.dto.response.SaleResponseDTO;
import br.com.system.enums.SaleStatus;
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

@Tag(name = "Sale", description = "Endpoints for sale management.")
public interface SaleApi {
    @Operation(summary = "List sales", description = "Returns all sales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<SaleResponseDTO> findAll();

    @Operation(summary = "List sales by status", description = "Returns sales filtered by status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SaleResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<SaleResponseDTO> findByStatus(@PathVariable("status") SaleStatus status);

    @Operation(summary = "List sales by administrator", description = "Returns sales linked to the informed administrator.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SaleResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Administrator not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<SaleResponseDTO> findByAdmin(@PathVariable("adminId") Long adminId);

    @Operation(summary = "List sales by client", description = "Returns sales linked to the informed client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SaleResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<SaleResponseDTO> findByClient(@PathVariable("clientId") Long clientId);

    @Operation(summary = "Get sale by id", description = "Returns the sale for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    SaleResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "Create sale", description = "Creates a new sale.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sale created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<SaleResponseDTO> create(@RequestBody SaleRequestDTO sale);

    @Operation(summary = "Update sale", description = "Updates the sale.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    SaleResponseDTO update(@PathVariable("id") Long id, @RequestBody SaleRequestDTO sale);

    @Operation(summary = "Cancel sale", description = "Cancels the sale.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    SaleResponseDTO cancel(@PathVariable("id") Long id);

    @Operation(summary = "Delete sale", description = "Deletes the sale.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sale deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long id);
}
