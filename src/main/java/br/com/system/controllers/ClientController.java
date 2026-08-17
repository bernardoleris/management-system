package br.com.system.controllers;

import br.com.system.controllers.api.ClientApi;
import br.com.system.data.dto.request.ClientRequestDTO;
import br.com.system.data.dto.response.ClientResponseDTO;
import br.com.system.services.ClientServices;
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
@RequestMapping("/clients")
public class ClientController implements ClientApi {

    @Autowired
    private ClientServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Page<ClientResponseDTO> findAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ClientResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<ClientResponseDTO> create(@RequestBody @Valid ClientRequestDTO client) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(client));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ClientResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody @Valid ClientRequestDTO client) {
        return service.update(id, client);
    }

    @PatchMapping("/{id}/toggle-active")
    @Override
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}
