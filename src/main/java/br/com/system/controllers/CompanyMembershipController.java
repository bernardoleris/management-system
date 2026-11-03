package br.com.system.controllers;

import br.com.system.data.dto.request.CompanyMemberCreateRequestDTO;
import br.com.system.data.dto.request.CompanyMemberRoleRequestDTO;
import br.com.system.data.dto.request.ExistingCompanyMemberRequestDTO;
import br.com.system.data.dto.response.CompanyMemberResponseDTO;
import br.com.system.services.CompanyMembershipServices;
import jakarta.validation.Valid;
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
public class CompanyMembershipController {
    private final CompanyMembershipServices service;

    public CompanyMembershipController(CompanyMembershipServices service) {
        this.service = service;
    }

    @GetMapping
    public List<CompanyMemberResponseDTO> findAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<CompanyMemberResponseDTO> create(@RequestBody @Valid CompanyMemberCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/existing")
    public ResponseEntity<CompanyMemberResponseDTO> addExisting(
            @RequestBody @Valid ExistingCompanyMemberRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addExisting(dto));
    }

    @PutMapping("/{membershipId}/role")
    public CompanyMemberResponseDTO updateRole(
            @PathVariable Long membershipId,
            @RequestBody @Valid CompanyMemberRoleRequestDTO dto) {
        return service.updateRole(membershipId, dto);
    }

    @DeleteMapping("/{membershipId}")
    public ResponseEntity<Void> deactivate(@PathVariable Long membershipId) {
        service.deactivate(membershipId);
        return ResponseEntity.noContent().build();
    }
}
