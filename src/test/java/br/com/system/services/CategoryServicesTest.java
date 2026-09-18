package br.com.system.services;

import br.com.system.data.dto.request.CategoryRequestDTO;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServicesTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServices categoryServices;

    @Test
    void shouldRejectCategoryNameThatAlreadyExistsIgnoringCaseAndOuterSpaces() {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("  beverages  ");
        when(categoryRepository.existsByNameIgnoreCase("beverages")).thenReturn(true);

        assertThatThrownBy(() -> categoryServices.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category name already registered!");

        verify(categoryRepository).existsByNameIgnoreCase("beverages");
        verify(categoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
