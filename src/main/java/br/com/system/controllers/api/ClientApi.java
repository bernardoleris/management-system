package br.com.system.controllers.api;

import br.com.system.data.dto.request.ClientRequestDTO;
import br.com.system.data.dto.response.ClientResponseDTO;
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

@Tag(name = "Client", description = "Endpoints for client management.")
public interface ClientApi {
    @Operation(summary = "List clients", description = "Returns all clients.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<ClientResponseDTO> findAll();

    @Operation(summary = "Get client by id", description = "Returns the client for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ClientResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "Create client", description = "Creates a new client.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ClientResponseDTO> create(@RequestBody ClientRequestDTO client);

    @Operation(summary = "Update client", description = "Updates the client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ClientResponseDTO update(@PathVariable("id") Long id, @RequestBody ClientRequestDTO client);

    @Operation(summary = "Delete client", description = "Deletes the client.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long id);
}
