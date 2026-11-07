package br.com.system.controllers.api;

import br.com.system.data.dto.response.UserEntityResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "User", description = "Read-only directory of users linked to the active company.")
public interface UserEntityApi {
    @Operation(summary = "List users", description = "Returns users linked to the active company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<UserEntityResponseDTO> findAll();

    @Operation(summary = "Get user by id", description = "Returns a user linked to the active company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserEntityResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    UserEntityResponseDTO findById(@PathVariable("id") Long id);

}
