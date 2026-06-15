package br.com.system.controllers;

import br.com.system.data.dto.request.StockMovementRequestDTO;
import br.com.system.data.dto.response.StockMovementResponseDTO;
import br.com.system.enums.MovementType;
import br.com.system.services.StockMovementServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    @Autowired
    private StockMovementServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StockMovementResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public StockMovementResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @GetMapping(
            value = "/product/{productId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<StockMovementResponseDTO> findByProduct(@PathVariable Long productId) {
        return service.findByProduct(productId);
    }

    @GetMapping(
            value = "/admin/{adminId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<StockMovementResponseDTO> findByAdmin(@PathVariable Long adminId) {
        return service.findByAdmin(adminId);
    }

    @GetMapping(
            value = "/supplier/{supplierId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<StockMovementResponseDTO> findBySupplier(@PathVariable Long supplierId) {
        return service.findBySupplier(supplierId);
    }

    @GetMapping(
            value = "/type/{type}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<StockMovementResponseDTO> findByType(@PathVariable MovementType type) {
        return service.findByType(type);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StockMovementResponseDTO> create(@RequestBody StockMovementRequestDTO movement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(movement));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public StockMovementResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody StockMovementRequestDTO movement) {
        return service.update(id, movement);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
