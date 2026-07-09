package br.com.system.controllers;

import br.com.system.controllers.api.SaleItemApi;
import br.com.system.data.dto.request.SaleItemRequestDTO;
import br.com.system.data.dto.response.SaleItemResponseDTO;
import br.com.system.services.SaleItemServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales/{saleId}/items")
public class SaleItemController implements SaleItemApi {

    @Autowired
    private SaleItemServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<SaleItemResponseDTO> findBySale(@PathVariable Long saleId) {
        return service.findBySale(saleId);
    }

    @GetMapping(
            value = "/{itemId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public SaleItemResponseDTO findById(
            @PathVariable Long saleId,
            @PathVariable Long itemId) {
        return service.findById(saleId, itemId);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<SaleItemResponseDTO> create(
            @PathVariable Long saleId,
            @RequestBody SaleItemRequestDTO item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(saleId, item));
    }

    @PutMapping(
            value = "/{itemId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public SaleItemResponseDTO update(
            @PathVariable Long saleId,
            @PathVariable Long itemId,
            @RequestBody SaleItemRequestDTO item) {
        return service.update(saleId, itemId, item);
    }

    @DeleteMapping(value = "/{itemId}")
    @Override
    public ResponseEntity<?> delete(
            @PathVariable Long saleId,
            @PathVariable Long itemId) {
        service.delete(saleId, itemId);
        return ResponseEntity.noContent().build();
    }
}
