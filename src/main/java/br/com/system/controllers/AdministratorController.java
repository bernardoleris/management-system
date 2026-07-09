package br.com.system.controllers;

import br.com.system.data.dto.request.AdministratorRequestDTO;
import br.com.system.data.dto.response.AdministratorResponseDTO;
import br.com.system.controllers.api.AdministratorApi;
import br.com.system.services.AdministratorServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/administrators")
public class AdministratorController implements AdministratorApi {

    @Autowired
    private AdministratorServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<AdministratorResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public AdministratorResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<AdministratorResponseDTO> create(@RequestBody AdministratorRequestDTO administrator) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(administrator));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public AdministratorResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody AdministratorRequestDTO administrator) {
        return service.update(id, administrator);
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
