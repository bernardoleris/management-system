package br.com.system.services;

import br.com.system.data.dto.request.SupplierRequestDTO;
import br.com.system.data.dto.response.SupplierResponseDTO;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Supplier;
import br.com.system.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class SupplierServices {

    private final Logger logger = Logger.getLogger(SupplierServices.class.getName());

    @Autowired
    private SupplierRepository supplierRepository;

    public List<SupplierResponseDTO> findAll() {
        logger.info("Finding all suppliers!");

        return ObjectMapper.parseListObjects(supplierRepository.findAll(), SupplierResponseDTO.class);
    }

    public List<SupplierResponseDTO> findAllActive() {
        logger.info("Finding all active suppliers!");

        return supplierRepository.findAll()
                .stream()
                .filter(s -> s.getActive())
                .map(s -> ObjectMapper.parseObject(s, SupplierResponseDTO.class))
                .toList();
    }

    public List<SupplierResponseDTO> findAllDisabled() {
        logger.info("Finding all active suppliers!");

        return supplierRepository.findAll()
                .stream()
                .filter(s -> !s.getActive())
                .map(s -> ObjectMapper.parseObject(s, SupplierResponseDTO.class))
                .toList();
    }

    public SupplierResponseDTO findById(Long id) {
        logger.info("Finding one supplier!");

        Supplier entity = findEntityById(id);

        return ObjectMapper.parseObject(entity, SupplierResponseDTO.class);
    }

    public SupplierResponseDTO create(SupplierRequestDTO dto) {
        logger.info("Creating one supplier!");

        if (supplierRepository.existsByCnpj(dto.getCnpj())) {
            throw new DuplicateResourceException("CNPJ already registered!");
        }

        Supplier entity = new Supplier();
        setSupplierFields(entity, dto);

        return ObjectMapper.parseObject(supplierRepository.save(entity), SupplierResponseDTO.class);
    }

    public SupplierResponseDTO update(Long id, SupplierRequestDTO dto) {
        logger.info("Updating one supplier!");

        Supplier entity = findEntityById(id);

        if (supplierRepository.existsByCnpjAndIdNot(dto.getCnpj(), id)) {
            throw new DuplicateResourceException("CNPJ already registered!");
        }

        setSupplierFields(entity, dto);

        return ObjectMapper.parseObject(supplierRepository.save(entity), SupplierResponseDTO.class);
    }

    public void toggleActive(Long id) {
        logger.info("Toggling active status of supplier!");

        Supplier entity = findEntityById(id);
        entity.setActive(!entity.getActive());
        supplierRepository.save(entity);
    }

    // ─── Métodos internos ────────────────────────────────────────────────────

    private Supplier findEntityById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No supplier found for this ID!"));
    }

    private void setSupplierFields(Supplier entity, SupplierRequestDTO dto) {
        entity.setTradeName(dto.getTradeName());
        entity.setCnpj(dto.getCnpj());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
    }
}