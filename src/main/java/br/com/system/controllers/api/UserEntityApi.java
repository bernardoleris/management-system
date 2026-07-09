package br.com.system.controllers.api;

import br.com.system.data.dto.request.UserEntityRequestDTO;
import br.com.system.data.dto.response.UserEntityResponseDTO;
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

@Tag(name = "User", description = "Endpoints for user management.")
public interface UserEntityApi {
    @Operation(summary = "List users", description = "Returns all users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<UserEntityResponseDTO> findAll();

    @Operation(summary = "Get user by id", description = "Returns the user for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserEntityResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    UserEntityResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "Create user", description = "Creates a new user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserEntityResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<UserEntityResponseDTO> create(@RequestBody UserEntityRequestDTO user);

    @Operation(summary = "Update user", description = "Updates the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserEntityResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    UserEntityResponseDTO update(@PathVariable("id") Long id, @RequestBody UserEntityRequestDTO user);

    @Operation(summary = "Delete user", description = "Deletes the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("id") Long id);
}
