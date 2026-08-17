package br.com.system.controllers;

import br.com.system.controllers.api.ProductApi;
import br.com.system.data.dto.request.ProductRequestDTO;
import br.com.system.data.dto.response.ProductResponseDTO;
import br.com.system.services.ProductServices;
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
@RequestMapping("/products")
public class ProductController implements ProductApi {

    @Autowired
    private ProductServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<ProductResponseDTO> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<ProductResponseDTO> findActive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findActive(pageable);
    }

    @GetMapping(value = "/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<ProductResponseDTO> findByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findByCategory(categoryId, pageable);
    }

    @GetMapping(value = "/brand/{brandId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<ProductResponseDTO> findByBrand(
            @PathVariable Long brandId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findByBrand(brandId, pageable);
    }

    @GetMapping(value = "/supplier/{supplierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<ProductResponseDTO> findBySupplier(
            @PathVariable Long supplierId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findBySupplier(supplierId, pageable);
    }

    @GetMapping(value = "/barcode/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ProductResponseDTO findByBarcode(@PathVariable String barcode) {
        return service.findByBarcode(barcode);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ProductResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid ProductRequestDTO product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(product));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ProductResponseDTO update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDTO product) {
        return service.update(id, product);
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @Override
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}