package br.com.system.controllers;

import br.com.system.data.dto.request.UserEntityRequestDTO;
import br.com.system.data.dto.response.UserEntityResponseDTO;
import br.com.system.services.UserEntityServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserEntityController {

    @Autowired
    private UserEntityServices service;

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<UserEntityResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserEntityResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserEntityResponseDTO> create(@RequestBody UserEntityRequestDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserEntityResponseDTO update(
            @PathVariable("id") Long id,
            @RequestBody UserEntityRequestDTO user) {
        return service.update(id, user);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
