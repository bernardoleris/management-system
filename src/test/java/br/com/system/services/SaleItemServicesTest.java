package br.com.system.services;

import br.com.system.data.dto.request.SaleItemRequestDTO;
import br.com.system.enums.SaleStatus;
import br.com.system.model.Product;
import br.com.system.model.Sale;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleItemServicesTest {
    @Mock private SaleRepository saleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryService inventoryService;
    @Mock private StockMovementServices stockMovementServices;
    @InjectMocks private SaleItemServices saleItemServices;

    @Test
    void shouldDecreaseStockRecalculateSaleAndSynchronizeMovementForCompletedSaleItem() {
        Sale sale = new Sale();
        sale.setId(10L);
        sale.setStatus(SaleStatus.COMPLETED);
        Product product = new Product();
        product.setSalePrice(new BigDecimal("20.00"));
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        SaleItemRequestDTO request = new SaleItemRequestDTO();
        request.setProductId(1L);
        request.setQuantity(2);
        saleItemServices.create(10L, request);

        assertThat(sale.getTotalValue()).isEqualByComparingTo("40.00");
        verify(inventoryService).decrease(product, 2);
        verify(saleRepository).save(sale);
        verify(stockMovementServices).synchronizeFromSale(sale);
    }
}
