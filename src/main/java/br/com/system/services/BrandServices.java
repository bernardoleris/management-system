package br.com.system.services;

import br.com.system.data.dto.request.BrandRequestDTO;
import br.com.system.data.dto.response.BrandResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Brand;
import br.com.system.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class BrandServices {
    private final Logger logger = Logger.getLogger(BrandServices.class.getName());

    @Autowired
    private BrandRepository brandRepository;

    public List<BrandResponseDTO> findAll() {
        logger.info("Finding brands!");

        return ObjectMapper.parseListObjects(brandRepository.findAll(), BrandResponseDTO.class);
    }

    public BrandResponseDTO findById(Long id) {
        logger.info("Finding one brand!");

        Brand entity = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No brand found for this ID!"));

        return ObjectMapper.parseObject(entity, BrandResponseDTO.class);
    }

    public BrandResponseDTO create(BrandRequestDTO brand) {
        logger.info("Creating one brand!");

        Brand entity = ObjectMapper.parseObject(brand, Brand.class);

        return ObjectMapper.parseObject(brandRepository.save(entity), BrandResponseDTO.class);
    }

    public BrandResponseDTO update(Long id, BrandRequestDTO brand) {
        logger.info("Updating one brand!");

        Brand entity = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No brand found for this ID!"));

        entity.setName(brand.getName());

        return ObjectMapper.parseObject(brandRepository.save(entity), BrandResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one brand!");

        Brand entity = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No brand found for this ID!"));

        brandRepository.delete(entity);
    }
}
