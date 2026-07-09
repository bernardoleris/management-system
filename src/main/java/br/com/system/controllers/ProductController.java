package br.com.system.controllers;

import br.com.system.controllers.api.ProductApi;
import br.com.system.data.dto.request.ProductRequestDTO;
import br.com.system.data.dto.response.ProductResponseDTO;
import br.com.system.services.ProductServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController implements ProductApi {

    @Autowired
    private ProductServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<ProductResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/active",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<ProductResponseDTO> findActive() {
        return service.findActive();
    }

    @GetMapping(
            value = "/category/{categoryId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<ProductResponseDTO> findByCategory(@PathVariable Long categoryId) {
        return service.findByCategory(categoryId);
    }

    @GetMapping(
            value = "/brand/{brandId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<ProductResponseDTO> findByBrand(@PathVariable Long brandId) {
        return service.findByBrand(brandId);
    }

    @GetMapping(
            value = "/supplier/{supplierId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<ProductResponseDTO> findBySupplier(@PathVariable Long supplierId) {
        return service.findBySupplier(supplierId);
    }

    @GetMapping(
            value = "/barcode/{barcode}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ProductResponseDTO findByBarcode(@PathVariable String barcode) {
        return service.findByBarcode(barcode);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ProductResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<ProductResponseDTO> create(@RequestBody ProductRequestDTO product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(product));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ProductResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody ProductRequestDTO product) {
        return service.update(id, product);
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
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
