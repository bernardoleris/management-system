package br.com.system.controllers;

import br.com.system.controllers.api.SaleApi;
import br.com.system.data.dto.request.SaleRequestDTO;
import br.com.system.data.dto.response.SaleResponseDTO;
import br.com.system.enums.SaleStatus;
import br.com.system.services.SaleServices;
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
@RequestMapping("/sales")
public class SaleController implements SaleApi {

    @Autowired
    private SaleServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<SaleResponseDTO> findAll(
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping(
            value = "/status/{status}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public Page<SaleResponseDTO> findByStatus(
            @PathVariable SaleStatus status,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findByStatus(status, pageable);
    }

    @GetMapping(
            value = "/admin/{adminId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public Page<SaleResponseDTO> findByAdmin(
            @PathVariable Long adminId,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findByAdmin(adminId, pageable);
    }

    @GetMapping(
            value = "/client/{clientId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public Page<SaleResponseDTO> findByClient(
            @PathVariable Long clientId,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findByClient(clientId, pageable);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public SaleResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<SaleResponseDTO> create(@RequestBody @Valid SaleRequestDTO sale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(sale));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public SaleResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody @Valid SaleRequestDTO sale) {
        return service.update(id, sale);
    }

    @PatchMapping(
            value = "/{id}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public SaleResponseDTO cancel(@PathVariable("id") Long id) {
        return service.cancel(id);
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
