package br.com.system.controllers;

import br.com.system.data.dto.request.LoginRequestDTO;
import br.com.system.data.dto.request.RefreshTokenRequestDTO;
import br.com.system.data.dto.request.SwitchCompanyRequestDTO;
import br.com.system.data.dto.response.AvailableCompanyResponseDTO;
import br.com.system.data.dto.response.LoginResponseDTO;
import br.com.system.data.dto.response.SwitchCompanyResponseDTO;
import br.com.system.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Authentication", description = "Endpoints for authentication.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @Operation(summary = "Login", description = "Authenticates the user and returns access and refresh tokens for the first active company.")
    @SecurityRequirements
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
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        return ResponseEntity.ok(service.refreshToken(dto.getRefreshToken(), dto.getCompanyId()));
    }

    @Operation(summary = "Logout", description = "Revokes the refresh token for the authenticated user.")
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

    @Operation(summary = "List available companies", description = "Lists active companies available to the authenticated user. Use this after login or before switching the active company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available companies retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    @GetMapping("/companies")
    public List<AvailableCompanyResponseDTO> availableCompanies() {
        return service.availableCompanies();
    }

    @Operation(summary = "Switch active company", description = "Issues a new access token scoped to the selected active company. The client must replace its current access token with the returned token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active company switched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwitchCompanyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "No active access to the requested company", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    @PostMapping("/switch-company")
    public ResponseEntity<SwitchCompanyResponseDTO> switchCompany(
            @RequestBody @Valid SwitchCompanyRequestDTO dto) {
        return ResponseEntity.ok(service.switchCompany(dto.getCompanyId()));
    }
}
