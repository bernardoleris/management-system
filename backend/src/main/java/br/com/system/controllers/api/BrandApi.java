package br.com.system.controllers.api;

import br.com.system.data.dto.request.BrandRequestDTO;
import br.com.system.data.dto.response.BrandResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Brand", description = "Endpoints for brand management.")
public interface BrandApi {
    @Operation(summary = "List brands", description = "Returns all brands.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brands found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<BrandResponseDTO> findAll();

    @Operation(summary = "Get brand by id", description = "Returns the brand for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brand found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    BrandResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "Create brand", description = "Creates a new brand.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Brand created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<BrandResponseDTO> create(@RequestBody BrandRequestDTO brand);

    @Operation(summary = "Update brand", description = "Updates the brand.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brand updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    BrandResponseDTO update(@PathVariable("id") Long id, @RequestBody BrandRequestDTO brand);

    @Operation(summary = "Delete brand", description = "Deletes the brand.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Brand deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long id);
}
