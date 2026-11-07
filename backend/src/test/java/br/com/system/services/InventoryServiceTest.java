package br.com.system.services;

import br.com.system.exception.InsufficientStockException;
import br.com.system.model.Product;
import br.com.system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldRejectDecreaseWhenRequestedQuantityExceedsAvailableStock() {
        Product product = new Product();
        product.setName("Coffee");
        product.setQuantity(3);

        assertThatThrownBy(() -> inventoryService.decrease(product, 4))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Insufficient stock for product: Coffee");

        assertThat(product.getQuantity()).isEqualTo(3);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldDecreaseStockAndPersistProductWhenQuantityIsAvailable() {
        Product product = new Product();
        product.setQuantity(5);

        inventoryService.decrease(product, 2);

        assertThat(product.getQuantity()).isEqualTo(3);
        verify(productRepository).save(product);
    }
}
