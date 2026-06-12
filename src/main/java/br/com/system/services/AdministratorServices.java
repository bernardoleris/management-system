package br.com.system.services;

import br.com.system.data.dto.request.AdministratorRequestDTO;
import br.com.system.data.dto.response.AdministratorResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Administrator;
import br.com.system.model.UserEntity;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class AdministratorServices {
    private final Logger logger = Logger.getLogger(AdministratorServices.class.getName());

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private UserEntityRepository userRepository;

    public List<AdministratorResponseDTO> findAll() {
        logger.info("Finding administrators!");

        return ObjectMapper.parseListObjects(administratorRepository.findAll(), AdministratorResponseDTO.class);
    }

    public AdministratorResponseDTO findById(Long id) {
        logger.info("Finding one administrator!");

        Administrator entity = administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));

        return ObjectMapper.parseObject(entity, AdministratorResponseDTO.class);
    }

    public AdministratorResponseDTO create(AdministratorRequestDTO administrator) {
        logger.info("Creating one administrator!");

        Administrator entity = new Administrator();
        setAdministratorFields(entity, administrator);

        return ObjectMapper.parseObject(administratorRepository.save(entity), AdministratorResponseDTO.class);
    }

    public AdministratorResponseDTO update(Long id, AdministratorRequestDTO administrator) {
        logger.info("Updating one administrator!");

        Administrator entity = administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));

        setAdministratorFields(entity, administrator);

        return ObjectMapper.parseObject(administratorRepository.save(entity), AdministratorResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one administrator!");

        Administrator entity = administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));

        administratorRepository.delete(entity);
    }

    private void setAdministratorFields(Administrator entity, AdministratorRequestDTO administrator) {
        UserEntity user = userRepository.findById(administrator.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No user found for this ID!"));

        entity.setUser(user);
        entity.setLogin(administrator.getLogin());
        entity.setPassword(administrator.getPassword());
        entity.setRole(administrator.getRole());
        entity.setLastLogin(administrator.getLastLogin());
    }
}
