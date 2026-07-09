package br.com.system.controllers;

import br.com.system.controllers.api.AlertApi;
import br.com.system.data.dto.request.AlertRequestDTO;
import br.com.system.data.dto.response.AlertResponseDTO;
import br.com.system.services.AlertServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController implements AlertApi {

    @Autowired
    private AlertServices service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<AlertResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/active",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<AlertResponseDTO> findActive() {
        return service.findActive();
    }

    @GetMapping(
            value = "/unread",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<AlertResponseDTO> findUnread() {
        return service.findUnread();
    }

    @GetMapping(
            value = "/admin/{adminId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<AlertResponseDTO> findByAdmin(@PathVariable Long adminId) {
        return service.findByAdmin(adminId);
    }

    @GetMapping(
            value = "/product/{productId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<AlertResponseDTO> findByProduct(@PathVariable Long productId) {
        return service.findByProduct(productId);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public AlertResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<AlertResponseDTO> create(@RequestBody AlertRequestDTO alert) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(alert));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public AlertResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody AlertRequestDTO alert) {
        return service.update(id, alert);
    }

    @PatchMapping(
            value = "/{id}/read",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public AlertResponseDTO markAsRead(@PathVariable("id") Long id) {
        return service.markAsRead(id);
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
