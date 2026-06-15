package br.com.system.controllers;

import br.com.system.data.dto.request.BrandRequestDTO;
import br.com.system.data.dto.response.BrandResponseDTO;
import br.com.system.services.BrandServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BrandResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BrandResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BrandResponseDTO> create(@RequestBody BrandRequestDTO brand) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(brand));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BrandResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody BrandRequestDTO brand) {
        return service.update(id, brand);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
