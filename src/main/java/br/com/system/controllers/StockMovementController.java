package br.com.system.controllers;

import br.com.system.controllers.api.StockMovementApi;
import br.com.system.data.dto.request.StockMovementRequestDTO;
import br.com.system.data.dto.response.StockMovementResponseDTO;
import br.com.system.enums.MovementType;
import br.com.system.services.StockMovementServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController implements StockMovementApi {

    @Autowired
    private StockMovementServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<StockMovementResponseDTO> findAll(
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public StockMovementResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping(value = "/admin/{adminId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<StockMovementResponseDTO> findByAdmin(
            @PathVariable Long adminId,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findByAdmin(adminId, pageable);
    }

    @GetMapping(value = "/supplier/{supplierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<StockMovementResponseDTO> findBySupplier(
            @PathVariable Long supplierId,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findBySupplier(supplierId, pageable);
    }

    @GetMapping(value = "/type/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<StockMovementResponseDTO> findByType(
            @PathVariable MovementType type,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findByType(type, pageable);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<StockMovementResponseDTO> create(@RequestBody @Valid StockMovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
