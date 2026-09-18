package br.com.system.services;

import br.com.system.data.dto.request.CategoryRequestDTO;
import br.com.system.data.dto.response.CategoryResponseDTO;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Category;
import br.com.system.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class CategoryServices {
    private final Logger logger = Logger.getLogger(CategoryServices.class.getName());

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponseDTO> findAll() {
        logger.info("Finding categories!");

        return ObjectMapper.parseListObjects(categoryRepository.findAll(), CategoryResponseDTO.class);
    }

    public CategoryResponseDTO findById(Long id) {
        logger.info("Finding one category!");

        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this ID!"));

        return ObjectMapper.parseObject(entity, CategoryResponseDTO.class);
    }

    public CategoryResponseDTO create(CategoryRequestDTO category) {
        logger.info("Creating one category!");

        String name = normalizeName(category.getName());
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category name already registered!");
        }

        Category entity = ObjectMapper.parseObject(category, Category.class);
        entity.setName(name);

        return ObjectMapper.parseObject(categoryRepository.save(entity), CategoryResponseDTO.class);
    }

    public CategoryResponseDTO update(Long id, CategoryRequestDTO category) {
        logger.info("Updating one category!");

        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this ID!"));

        String name = normalizeName(category.getName());
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Category name already registered!");
        }

        entity.setName(name);

        return ObjectMapper.parseObject(categoryRepository.save(entity), CategoryResponseDTO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one category!");

        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this ID!"));

        categoryRepository.delete(entity);
    }

    private String normalizeName(String name) {
        return name.strip();
    }
}
