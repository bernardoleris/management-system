package br.com.system.controllers;

import br.com.system.data.dto.request.AddressRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import br.com.system.services.AddressServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients/{id}/address")
@Tag(name = "Address", description = "Endpoints for client address management.")
public class AddressController {
    @Autowired
    private AddressServices service;

    @GetMapping
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
    public ResponseEntity<AddressResponseDTO> findByClient(@PathVariable("id") Long clientId) {
        return ResponseEntity.ok(service.findByClient(clientId));
    }

    @PostMapping
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
    public ResponseEntity<AddressResponseDTO> create(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(clientId, dto));
    }

    @DeleteMapping
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
    public ResponseEntity<?> delete(@PathVariable("id") Long clientId) {
        service.delete(clientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
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
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(service.update(clientId, dto));
    }
}
