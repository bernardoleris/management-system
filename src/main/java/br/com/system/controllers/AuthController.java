package br.com.system.controllers;

import br.com.system.data.dto.request.LoginRequestDTO;
import br.com.system.data.dto.request.RefreshTokenRequestDTO;
import br.com.system.data.dto.response.LoginResponseDTO;
import br.com.system.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for authentication.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Operation(summary = "Login", description = "Authenticates the administrator and returns a JWT token and refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid login or password", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @Operation(summary = "Refresh token", description = "Generates a new access token using the refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        return ResponseEntity.ok(service.refreshToken(dto.getRefreshToken()));
    }

    @Operation(summary = "Logout", description = "Revokes the refresh token and logs out the administrator.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logged out successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        service.logout(dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}