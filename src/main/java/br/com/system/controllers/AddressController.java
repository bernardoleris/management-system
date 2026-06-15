package br.com.system.controllers;

import br.com.system.data.dto.request.AddressRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import br.com.system.services.AddressServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients/{id}/address")
public class AddressController {
    @Autowired
    private AddressServices service;

    @GetMapping
    public ResponseEntity<AddressResponseDTO> findByClient(@PathVariable("id") Long clientId) {
        return ResponseEntity.ok(service.findByClient(clientId));
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> create(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(clientId, dto));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@PathVariable("id") Long clientId) {
        service.delete(clientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(service.update(clientId, dto));
    }
}
