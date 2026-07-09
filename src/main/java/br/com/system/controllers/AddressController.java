package br.com.system.controllers;

import br.com.system.controllers.api.AddressApi;
import br.com.system.data.dto.request.AddressRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import br.com.system.services.AddressServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients/{id}/address")
public class AddressController implements AddressApi {
    @Autowired
    private AddressServices service;

    @GetMapping
    @Override
    public ResponseEntity<AddressResponseDTO> findByClient(@PathVariable("id") Long clientId) {
        return ResponseEntity.ok(service.findByClient(clientId));
    }

    @PostMapping
    @Override
    public ResponseEntity<AddressResponseDTO> create(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(clientId, dto));
    }

    @DeleteMapping
    @Override
    public ResponseEntity<?> delete(@PathVariable("id") Long clientId) {
        service.delete(clientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    @Override
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable("id") Long clientId,
            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(service.update(clientId, dto));
    }
}
