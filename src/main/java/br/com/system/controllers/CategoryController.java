package br.com.system.controllers;

import br.com.system.data.dto.request.CategoryRequestDTO;
import br.com.system.data.dto.response.CategoryResponseDTO;
import br.com.system.services.CategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CategoryResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CategoryResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CategoryResponseDTO> create(@RequestBody CategoryRequestDTO category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(category));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CategoryResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody CategoryRequestDTO category) {
        return service.update(id, category);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
