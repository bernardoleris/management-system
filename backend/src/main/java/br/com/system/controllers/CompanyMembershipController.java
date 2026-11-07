package br.com.system.controllers;

import br.com.system.data.dto.request.CompanyMemberCreateRequestDTO;
import br.com.system.data.dto.request.CompanyMemberRoleRequestDTO;
import br.com.system.data.dto.request.ExistingCompanyMemberRequestDTO;
import br.com.system.data.dto.response.CompanyMemberResponseDTO;
import br.com.system.services.CompanyMembershipServices;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/company/members")
@Tag(name = "Company members", description = "Manage user memberships and roles in the active company. Owner access is required.")
public class CompanyMembershipController {
    private final CompanyMembershipServices service;

    public CompanyMembershipController(CompanyMembershipServices service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List company members", description = "Returns active and inactive memberships for the active company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company members retrieved",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CompanyMemberResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Owner role required", content = @Content)
    })
    public List<CompanyMemberResponseDTO> findAll() {
        return service.findAll();
    }

    @PostMapping
    @Operation(summary = "Create company member", description = "Creates a user account and adds it to the active company with the selected role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyMemberResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid member data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Owner role required", content = @Content),
            @ApiResponse(responseCode = "409", description = "Login or email already in use", content = @Content)
    })
    public ResponseEntity<CompanyMemberResponseDTO> create(@RequestBody @Valid CompanyMemberCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/existing")
    @Operation(summary = "Add existing user to company", description = "Adds an existing account to the active company with the selected role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Existing user added",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyMemberResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid membership data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Owner role required", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "User already belongs to this company", content = @Content)
    })
    public ResponseEntity<CompanyMemberResponseDTO> addExisting(
            @RequestBody @Valid ExistingCompanyMemberRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addExisting(dto));
    }

    @PutMapping("/{membershipId}/role")
    @Operation(summary = "Change company member role", description = "Updates a member's role within the active company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member role updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyMemberResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Owner role required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membership not found", content = @Content)
    })
    public CompanyMemberResponseDTO updateRole(
            @PathVariable Long membershipId,
            @RequestBody @Valid CompanyMemberRoleRequestDTO dto) {
        return service.updateRole(membershipId, dto);
    }

    @DeleteMapping("/{membershipId}")
    @Operation(summary = "Deactivate company member", description = "Deactivates a user's membership in the active company without deleting the account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membership deactivated", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Owner role required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membership not found", content = @Content)
    })
    public ResponseEntity<Void> deactivate(@PathVariable Long membershipId) {
        service.deactivate(membershipId);
        return ResponseEntity.noContent().build();
    }
}
