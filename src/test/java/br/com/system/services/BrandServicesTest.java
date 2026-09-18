package br.com.system.services;

import br.com.system.data.dto.request.BrandRequestDTO;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServicesTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandServices brandServices;

    @Test
    void shouldRejectBrandNameThatAlreadyExistsIgnoringCaseAndOuterSpaces() {
        BrandRequestDTO request = new BrandRequestDTO();
        request.setName("  acme  ");
        when(brandRepository.existsByNameIgnoreCase("acme")).thenReturn(true);

        assertThatThrownBy(() -> brandServices.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Brand name already registered!");

        verify(brandRepository).existsByNameIgnoreCase("acme");
        verify(brandRepository, never()).save(any());
    }
}
