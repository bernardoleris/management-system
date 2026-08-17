package br.com.system.controllers;

import br.com.system.controllers.api.SupplierApi;
import br.com.system.data.dto.request.SupplierRequestDTO;
import br.com.system.data.dto.response.SupplierResponseDTO;
import br.com.system.services.SupplierServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController implements SupplierApi {

    private final SupplierServices service;

    @PostMapping
    @Override
    public ResponseEntity<SupplierResponseDTO> create(@RequestBody @Valid SupplierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<SupplierResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid SupplierRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/toggle-active")
    @Override
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<SupplierResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    @Override
    public Page<SupplierResponseDTO> findAll(
            @PageableDefault(size = 20, sort = "tradeName") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/active")
    @Override
    public Page<SupplierResponseDTO> findAllActive(
            @PageableDefault(size = 20, sort = "tradeName") Pageable pageable) {
        return service.findAllActive(pageable);
    }

    @GetMapping("/disabled")
    @Override
    public Page<SupplierResponseDTO> findAllDisabled(
            @PageableDefault(size = 20, sort = "tradeName") Pageable pageable) {
        return service.findAllDisabled(pageable);
    }
}
