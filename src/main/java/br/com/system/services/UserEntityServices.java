package br.com.system.services;

import br.com.system.data.dto.response.UserEntityResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.UserEntity;
import br.com.system.repository.UserEntityRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class UserEntityServices {
    private final Logger logger = Logger.getLogger(UserEntityServices.class.getName());

    @Autowired
    private UserEntityRepository repository;

    @Autowired
    private CurrentCompany currentCompany;

    public List<UserEntityResponseDTO> findAll() {
        logger.info("Finding users!");

        return ObjectMapper.parseListObjects(
                repository.findAllByCompanyId(currentCompany.getId()), UserEntityResponseDTO.class);
    }

    public UserEntityResponseDTO findById(Long id) {
        logger.info("Finding one user!");

        UserEntity entity = repository.findByIdAndCompanyId(id, currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No user found for this ID!"));

        return ObjectMapper.parseObject(entity, UserEntityResponseDTO.class);
    }
}
