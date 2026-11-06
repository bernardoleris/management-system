package br.com.system.services;

import br.com.system.data.dto.request.AddressRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Address;
import br.com.system.model.Client;
import br.com.system.repository.AddressRepository;
import br.com.system.repository.ClientRepository;
import br.com.system.repository.CompanyRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AddressServices {
    private final Logger logger = Logger.getLogger(AddressServices.class.getName());

    private final AddressRepository addressRepository;

    private final ClientRepository clientRepository;

    private final CompanyRepository companyRepository;

    private final CurrentCompany currentCompany;

    public AddressServices(
            AddressRepository addressRepository,
            ClientRepository clientRepository,
            CompanyRepository companyRepository,
            CurrentCompany currentCompany) {
        this.addressRepository = addressRepository;
        this.clientRepository = clientRepository;
        this.companyRepository = companyRepository;
        this.currentCompany = currentCompany;
    }

    public AddressResponseDTO findByClient(Long clientId) {
        logger.info("Finding address by client!");

        Client client = findClient(clientId);
        return ObjectMapper.parseObject(client.getAddress(), AddressResponseDTO.class);
    }

    public AddressResponseDTO create(Long clientId, AddressRequestDTO dto) {
        logger.info("Creating address for client!");

        Client client = findClient(clientId);
        Address address = ObjectMapper.parseObject(dto, Address.class);
        address.setCompany(companyRepository.getReferenceById(currentCompany.getId()));
        Address savedAddress = addressRepository.save(address);

        client.setAddress(savedAddress);
        clientRepository.save(client);

        return ObjectMapper.parseObject(savedAddress, AddressResponseDTO.class);
    }

    public AddressResponseDTO update(Long clientId, AddressRequestDTO dto) {
        logger.info("Updating address by client!");

        Client client = findClient(clientId);
        Address address = findAddress(client);

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
        Address address = findAddress(client);

        client.setAddress(null);
        clientRepository.save(client);
        addressRepository.delete(address);
    }

    private Client findClient(Long clientId) {
        return clientRepository.findByIdAndCompanyId(clientId, currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));
    }

    private Address findAddress(Client client) {
        if (client.getAddress() == null) {
            throw new ResourceNotFoundException("No address found for this client!");
        }

        return addressRepository.findByIdAndCompanyId(client.getAddress().getId(), currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No address found for this client!"));
    }
}
