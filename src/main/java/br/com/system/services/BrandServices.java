package br.com.system.services;

import br.com.system.data.dto.request.BrandRequestDTO;
import br.com.system.data.dto.response.BrandResponseDTO;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Brand;
import br.com.system.repository.BrandRepository;
import br.com.system.repository.CompanyRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class BrandServices {
    private final Logger logger = Logger.getLogger(BrandServices.class.getName());

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CurrentCompany currentCompany;

    public List<BrandResponseDTO> findAll() {
        logger.info("Finding brands!");

        return ObjectMapper.parseListObjects(
                brandRepository.findAllByCompanyId(currentCompany.getId()),
                BrandResponseDTO.class
        );
    }

    public BrandResponseDTO findById(Long id) {
        logger.info("Finding one brand!");

        return ObjectMapper.parseObject(findEntityById(id), BrandResponseDTO.class);
    }

    public BrandResponseDTO create(BrandRequestDTO brand) {
        logger.info("Creating one brand!");

        String name = normalizeName(brand.getName());
        Long companyId = currentCompany.getId();
        if (brandRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
            throw new DuplicateResourceException("Brand name already registered!");
        }

        Brand entity = ObjectMapper.parseObject(brand, Brand.class);
        entity.setName(name);
        entity.setCompany(companyRepository.getReferenceById(companyId));

        return ObjectMapper.parseObject(brandRepository.save(entity), BrandResponseDTO.class);
    }

    public BrandResponseDTO update(Long id, BrandRequestDTO brand) {
        logger.info("Updating one brand!");

        Brand entity = findEntityById(id);

        String name = normalizeName(brand.getName());
        if (brandRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(currentCompany.getId(), name, id)) {
            throw new DuplicateResourceException("Brand name already registered!");
        }

        entity.setName(name);

        return ObjectMapper.parseObject(brandRepository.save(entity), BrandResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one brand!");

        Brand entity = findEntityById(id);

        brandRepository.delete(entity);
    }

    private String normalizeName(String name) {
        return name.strip();
    }

    private Brand findEntityById(Long id) {
        return brandRepository.findByIdAndCompanyId(id, currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No brand found for this ID!"));
    }
}
