package br.com.system.controllers.api;

import br.com.system.data.dto.request.AddressRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AddressApi {
    @Operation(
            summary = "Get client address",
            description = "Returns the address registered for the informed client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Client or address not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<AddressResponseDTO> findByClient(@PathVariable("id") Long clientId);

    @Operation(
            summary = "Create client address",
            description = "Creates and links a new address to the informed client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<AddressResponseDTO> create(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto);

    @Operation(
            summary = "Delete client address",
            description = "Deletes the address currently associated with the informed client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Client or address not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long clientId);

    @Operation(
            summary = "Update client address",
            description = "Updates the address data associated with the informed client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Client or address not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<AddressResponseDTO> update(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto);
}
