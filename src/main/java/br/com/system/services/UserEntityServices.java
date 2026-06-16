package br.com.system.services;

import br.com.system.data.dto.request.UserEntityRequestDTO;
import br.com.system.data.dto.response.UserEntityResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.UserEntity;
import br.com.system.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class UserEntityServices {
    private final Logger logger = Logger.getLogger(UserEntityServices.class.getName());

    @Autowired
    private UserEntityRepository repository;

    public List<UserEntityResponseDTO> findAll() {
        logger.info("Finding users!");

        return ObjectMapper.parseListObjects(repository.findAll(), UserEntityResponseDTO.class);
    }

    public UserEntityResponseDTO findById(Long id) {
        logger.info("Finding one user!");

        UserEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user found for this ID!"));

        return ObjectMapper.parseObject(entity, UserEntityResponseDTO.class);
    }

    public UserEntityResponseDTO create(UserEntityRequestDTO user) {
        logger.info("Creating one user!");

        UserEntity entity = ObjectMapper.parseObject(user, UserEntity.class);
        UserEntity savedEntity = repository.save(entity);

        return ObjectMapper.parseObject(savedEntity, UserEntityResponseDTO.class);
    }

    public UserEntityResponseDTO update(Long id, UserEntityRequestDTO user) {
        logger.info("Updating one user!");

        UserEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user found for this ID!"));

        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());

        return ObjectMapper.parseObject(repository.save(entity), UserEntityResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one user!");

        UserEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user found for this ID!"));

        repository.delete(entity);
    }
}
