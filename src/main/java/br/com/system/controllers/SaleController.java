package br.com.system.controllers;

import br.com.system.data.dto.request.SaleRequestDTO;
import br.com.system.data.dto.response.SaleResponseDTO;
import br.com.system.enums.SaleStatus;
import br.com.system.services.SaleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SaleResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/status/{status}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<SaleResponseDTO> findByStatus(@PathVariable SaleStatus status) {
        return service.findByStatus(status);
    }

    @GetMapping(
            value = "/admin/{adminId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<SaleResponseDTO> findByAdmin(@PathVariable Long adminId) {
        return service.findByAdmin(adminId);
    }

    @GetMapping(
            value = "/client/{clientId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<SaleResponseDTO> findByClient(@PathVariable Long clientId) {
        return service.findByClient(clientId);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SaleResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SaleResponseDTO> create(@RequestBody SaleRequestDTO sale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(sale));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SaleResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody SaleRequestDTO sale) {
        return service.update(id, sale);
    }

    @PatchMapping(
            value = "/{id}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SaleResponseDTO cancel(@PathVariable("id") Long id) {
        return service.cancel(id);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
