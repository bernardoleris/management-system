package br.com.system.controllers;

import br.com.system.data.dto.request.LoginRequestDTO;
import br.com.system.data.dto.request.ChangePasswordRequestDTO;
import br.com.system.data.dto.request.ForgotPasswordRequestDTO;
import br.com.system.data.dto.request.EmailTokenRequestDTO;
import br.com.system.data.dto.request.OwnerRegistrationRequestDTO;
import br.com.system.data.dto.request.RefreshTokenRequestDTO;
import br.com.system.data.dto.request.ResendVerificationRequestDTO;
import br.com.system.data.dto.request.ResetPasswordRequestDTO;
import br.com.system.data.dto.request.SwitchCompanyRequestDTO;
import br.com.system.data.dto.response.AvailableCompanyResponseDTO;
import br.com.system.data.dto.response.LoginResponseDTO;
import br.com.system.data.dto.response.SwitchCompanyResponseDTO;
import br.com.system.services.AuthService;
import br.com.system.services.PasswordManagementService;
import br.com.system.services.OwnerRegistrationService;
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
    private final PasswordManagementService passwordManagementService;
    private final OwnerRegistrationService registrationService;

    public AuthController(AuthService service, PasswordManagementService passwordManagementService,
                          OwnerRegistrationService registrationService) {
        this.service = service;
        this.passwordManagementService = passwordManagementService;
        this.registrationService = registrationService;
    }

    @Operation(summary = "Register a company owner", description = "Creates a company and owner account in a pending state, then sends an email verification link. No plan or billing data is created.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Registration accepted; check the email inbox"),
            @ApiResponse(responseCode = "400", description = "Registration data is invalid", content = @Content),
            @ApiResponse(responseCode = "409", description = "Login or email already in use", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid OwnerRegistrationRequestDTO dto) {
        registrationService.register(dto);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Verify account email", description = "Consumes a single-use email verification token and activates the company owner account.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email verified"),
            @ApiResponse(responseCode = "400", description = "Verification token is invalid or expired", content = @Content)
    })
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid EmailTokenRequestDTO dto) {
        registrationService.verify(dto.getToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resend verification email", description = "Sends another verification link for a pending account. The response does not reveal whether an account exists.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted"),
            @ApiResponse(responseCode = "400", description = "Email is invalid", content = @Content)
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestBody @Valid ResendVerificationRequestDTO dto) {
        registrationService.resend(dto.getEmail());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Request password recovery", description = "Sends a one-time password reset link when the email belongs to an active account. The response is the same whether or not the account exists.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid email", content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO dto) {
        passwordManagementService.requestPasswordReset(dto.getEmail());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Reset password", description = "Changes the password using a valid single-use reset token.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Reset token is invalid, expired, or password is invalid", content = @Content)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO dto) {
        passwordManagementService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change password", description = "Changes the authenticated user's password after verifying the current password and revokes all refresh sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Current password is incorrect or new password is invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDTO dto) {
        passwordManagementService.changePassword(dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

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
