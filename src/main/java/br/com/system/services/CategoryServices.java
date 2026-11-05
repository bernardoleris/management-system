package br.com.system.services;

import br.com.system.data.dto.request.CategoryRequestDTO;
import br.com.system.data.dto.response.CategoryResponseDTO;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Category;
import br.com.system.repository.CompanyRepository;
import br.com.system.repository.CategoryRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class CategoryServices {
    private final Logger logger = Logger.getLogger(CategoryServices.class.getName());

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CurrentCompany currentCompany;

    public List<CategoryResponseDTO> findAll() {
        logger.info("Finding categories!");

        return ObjectMapper.parseListObjects(
                categoryRepository.findAllByCompanyId(currentCompany.getId()),
                CategoryResponseDTO.class
        );
    }

    public CategoryResponseDTO findById(Long id) {
        logger.info("Finding one category!");

        return ObjectMapper.parseObject(findEntityById(id), CategoryResponseDTO.class);
    }

    public CategoryResponseDTO create(CategoryRequestDTO category) {
        logger.info("Creating one category!");

        String name = normalizeName(category.getName());
        Long companyId = currentCompany.getId();
        if (categoryRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
            throw new DuplicateResourceException("Category name already registered!");
        }

        Category entity = ObjectMapper.parseObject(category, Category.class);
        entity.setName(name);
        entity.setCompany(companyRepository.getReferenceById(companyId));

        return ObjectMapper.parseObject(categoryRepository.save(entity), CategoryResponseDTO.class);
    }

    public CategoryResponseDTO update(Long id, CategoryRequestDTO category) {
        logger.info("Updating one category!");

        Category entity = findEntityById(id);

        String name = normalizeName(category.getName());
        if (categoryRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(currentCompany.getId(), name, id)) {
            throw new DuplicateResourceException("Category name already registered!");
        }

        entity.setName(name);

        return ObjectMapper.parseObject(categoryRepository.save(entity), CategoryResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one category!");

        Category entity = categoryRepository.findByIdAndCompanyId(id, currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this ID!"));

        categoryRepository.delete(entity);
    }

    private String normalizeName(String name) {
        return name.strip();
    }

    private Category findEntityById(Long id) {
        return categoryRepository.findByIdAndCompanyId(id, currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this ID!"));
    }
}
