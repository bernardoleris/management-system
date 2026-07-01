package br.com.system.services;

import br.com.system.data.dto.request.ClientRequestDTO;
import br.com.system.data.dto.response.ClientResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Address;
import br.com.system.model.Client;
import br.com.system.model.UserEntity;
import br.com.system.repository.AddressRepository;
import br.com.system.repository.ClientRepository;
import br.com.system.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class ClientServices {
    private final Logger logger = Logger.getLogger(ClientServices.class.getName());

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    public List<ClientResponseDTO> findAll() {
        logger.info("Finding clients!");

        return ObjectMapper.parseListObjects(clientRepository.findAll(), ClientResponseDTO.class);
    }

    public ClientResponseDTO findById(Long id) {
        logger.info("Finding one client!");

        Client entity = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));

        return ObjectMapper.parseObject(entity, ClientResponseDTO.class);
    }

    public ClientResponseDTO create(ClientRequestDTO client) {
        logger.info("Creating one client!");

        Client entity = new Client();
        setClientFields(entity, client);

        return ObjectMapper.parseObject(clientRepository.save(entity), ClientResponseDTO.class);
    }

    public ClientResponseDTO update(Long id, ClientRequestDTO client) {
        logger.info("Updating one client!");

        Client entity = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));

        setClientFields(entity, client);

        return ObjectMapper.parseObject(clientRepository.save(entity), ClientResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one client!");

        Client entity = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));

        clientRepository.delete(entity);
    }

    private void setClientFields(Client entity, ClientRequestDTO client) {
        UserEntity user = userRepository.findById(client.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No user found for this ID!"));

        if (client.getAddressId() != null) {
            Address address = addressRepository.findById(client.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("No address found for this ID!"));

            entity.setAddress(address);
        } else {
            entity.setAddress(null);
        }

        entity.setUser(user);
        entity.setDocumentType(client.getDocumentType());
        entity.setDocumentNumber(client.getDocumentNumber());
        entity.setBirthDate(client.getBirthDate());
    }
}
