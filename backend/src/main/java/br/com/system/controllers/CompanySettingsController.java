package br.com.system.controllers;

import br.com.system.data.dto.request.CompanySettingsRequestDTO;
import br.com.system.data.dto.response.CompanySettingsResponseDTO;
import br.com.system.services.CompanySettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company/settings")
@Tag(name = "Company settings", description = "Read and update settings for the active company. Owner access is required.")
public class CompanySettingsController {
    private final CompanySettingsService service;

    public CompanySettingsController(CompanySettingsService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get active company settings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company settings retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Owner role required")
    })
    public CompanySettingsResponseDTO findCurrent() {
        return service.findCurrent();
    }

    @PutMapping
    @Operation(summary = "Update active company settings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company settings updated"),
            @ApiResponse(responseCode = "400", description = "Invalid company settings"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Owner role required"),
            @ApiResponse(responseCode = "409", description = "Company document already registered")
    })
    public CompanySettingsResponseDTO update(@RequestBody @Valid CompanySettingsRequestDTO dto) {
        return service.update(dto);
    }
}
