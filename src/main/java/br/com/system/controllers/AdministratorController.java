package br.com.system.controllers;

import br.com.system.data.dto.request.AdministratorCreateRequestDTO;
import br.com.system.data.dto.request.AdministratorRequestDTO;
import br.com.system.data.dto.request.ChangePasswordRequestDTO;
import br.com.system.data.dto.response.AdministratorResponseDTO;
import br.com.system.controllers.api.AdministratorApi;
import br.com.system.services.AdministratorServices;
import jakarta.validation.Valid;
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
    public ResponseEntity<AdministratorResponseDTO> create(@RequestBody @Valid AdministratorCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public AdministratorResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody @Valid AdministratorRequestDTO administrator) {
        return service.update(id, administrator);
    }

    @PatchMapping("/{id}/toggle-active")
    @Override
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody @Valid ChangePasswordRequestDTO dto) {
        service.changePassword(id, dto);
        return ResponseEntity.noContent().build();
    }
}
