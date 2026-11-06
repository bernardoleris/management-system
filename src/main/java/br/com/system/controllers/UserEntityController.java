package br.com.system.controllers;

import br.com.system.controllers.api.UserEntityApi;
import br.com.system.data.dto.response.UserEntityResponseDTO;
import br.com.system.services.UserEntityServices;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserEntityController implements UserEntityApi {

    private final UserEntityServices service;

    public UserEntityController(UserEntityServices service) {
        this.service = service;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<UserEntityResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public UserEntityResponseDTO findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

}
