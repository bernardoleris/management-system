package br.com.system.controllers;

import br.com.system.controllers.api.StockMovementApi;
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
public class StockMovementController implements StockMovementApi {

    @Autowired
    private StockMovementServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<StockMovementResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public StockMovementResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping(value = "/admin/{adminId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<StockMovementResponseDTO> findByAdmin(@PathVariable Long adminId) {
        return service.findByAdmin(adminId);
    }

    @GetMapping(value = "/supplier/{supplierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<StockMovementResponseDTO> findBySupplier(@PathVariable Long supplierId) {
        return service.findBySupplier(supplierId);
    }

    @GetMapping(value = "/type/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<StockMovementResponseDTO> findByType(@PathVariable MovementType type) {
        return service.findByType(type);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<StockMovementResponseDTO> create(@RequestBody StockMovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
