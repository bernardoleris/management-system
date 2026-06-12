package br.com.system.services;

import br.com.system.data.dto.PersonDTO;
import br.com.system.data.dto.request.AddressRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Address;
import br.com.system.model.Client;
import br.com.system.repository.AddressRepository;
import br.com.system.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AddressServices {
    private final Logger logger = Logger.getLogger(AddressServices.class.getName());

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ClientRepository clientRepository;

    public AddressResponseDTO findByClient(Long clientId) {
        logger.info("Finding address by client!");

        Client client = findClient(clientId);
        return ObjectMapper.parseObject(client.getAddress(), AddressResponseDTO.class);
    }

    public AddressResponseDTO create(Long clientId, AddressRequestDTO dto) {
        logger.info("Creating address for client!");

        Client client = findClient(clientId);
        Address address = ObjectMapper.parseObject(dto, Address.class);
        Address savedAddress = addressRepository.save(address);

        client.setAddress(savedAddress);
        clientRepository.save(client);

        return ObjectMapper.parseObject(savedAddress, AddressResponseDTO.class);
    }

    public AddressResponseDTO update(Long clientId, AddressRequestDTO dto) {
        logger.info("Updating address by client!");

        Client client = findClient(clientId);
        Address address = client.getAddress();

        if (address == null) {
            throw new ResourceNotFoundException("No address found for this client!");
        }

        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());

        return ObjectMapper.parseObject(addressRepository.save(address), AddressResponseDTO.class);
    }

    public void delete(Long clientId) {
        logger.info("Deleting address by client!");

        Client client = findClient(clientId);
        Address address = client.getAddress();

        if (address == null) {
            throw new ResourceNotFoundException("No address found for this client!");
        }

        client.setAddress(null);
        clientRepository.save(client);
        addressRepository.delete(address);
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));
    }
}
