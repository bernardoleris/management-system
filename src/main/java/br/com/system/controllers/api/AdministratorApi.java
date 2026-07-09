package br.com.system.controllers.api;

import br.com.system.data.dto.request.AdministratorRequestDTO;
import br.com.system.data.dto.response.AdministratorResponseDTO;
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

@Tag(name = "Administrator", description = "Endpoints for administrator management.")
public interface AdministratorApi {

    @Operation(
            summary = "List administrators",
            description = "Returns all registered administrators."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrators found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AdministratorResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    List<AdministratorResponseDTO> findAll();

    @Operation(
            summary = "Get administrator by id",
            description = "Returns the administrator data for the informed id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrator found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdministratorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Administrator not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    AdministratorResponseDTO findById(@PathVariable("id") Long id);

    @Operation(
            summary = "Create administrator",
            description = "Creates a new administrator record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrator created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdministratorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<AdministratorResponseDTO> create(@RequestBody AdministratorRequestDTO administrator);

    @Operation(
            summary = "Update administrator",
            description = "Updates the data for the informed administrator."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrator updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdministratorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Administrator not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    AdministratorResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody AdministratorRequestDTO administrator);

    @Operation(
            summary = "Delete administrator",
            description = "Deletes the informed administrator."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Administrator deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Administrator not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long id);
}
