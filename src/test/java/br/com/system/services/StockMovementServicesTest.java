package br.com.system.services;

import br.com.system.enums.MovementType;
import br.com.system.data.dto.request.StockMovementItemRequestDTO;
import br.com.system.data.dto.request.StockMovementRequestDTO;
import br.com.system.exception.BusinessException;
import br.com.system.model.Administrator;
import br.com.system.model.Product;
import br.com.system.model.Sale;
import br.com.system.model.SaleItem;
import br.com.system.model.StockMovement;
import br.com.system.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServicesTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private StockMovementServices stockMovementServices;

    @Test
    void shouldCreateSaleMovementAndDecreaseStockForEachSaleItem() {
        Administrator administrator = new Administrator();
        Product product = new Product();
        product.setName("Coffee");

        Sale sale = new Sale();
        sale.setId(10L);
        sale.setAdmin(administrator);

        SaleItem saleItem = new SaleItem();
        saleItem.setProduct(product);
        saleItem.setQuantity(2);
        sale.getItems().add(saleItem);

        stockMovementServices.createFromSale(sale);

        verify(inventoryService).decrease(product, 2);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getType()).isEqualTo(MovementType.SALE);
        assertThat(savedMovement.getSale()).isSameAs(sale);
        assertThat(savedMovement.getAdmin()).isSameAs(administrator);
        assertThat(savedMovement.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getProduct()).isSameAs(product);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getStockMovement()).isSameAs(savedMovement);
        });
    }

    @Test
    void shouldRestoreStockWhenReversingExistingSaleMovement() {
        Product product = new Product();
        StockMovement movement = new StockMovement();
        movement.setType(MovementType.SALE);

        br.com.system.model.StockMovementItem movementItem = new br.com.system.model.StockMovementItem();
        movementItem.setProduct(product);
        movementItem.setQuantity(2);
        movement.getItems().add(movementItem);

        Sale sale = new Sale();
        sale.setId(10L);
        when(stockMovementRepository.findBySaleId(10L)).thenReturn(Optional.of(movement));

        boolean wasReversed = stockMovementServices.reverseFromSale(sale);

        assertThat(wasReversed).isTrue();
        verify(inventoryService).increase(product, 2);
    }

    @Test
    void shouldUpdateExistingSaleMovementInsteadOfCreatingAnotherOne() {
        Product product = new Product();
        Sale sale = new Sale();
        sale.setId(10L);

        SaleItem saleItem = new SaleItem();
        saleItem.setProduct(product);
        saleItem.setQuantity(3);
        sale.getItems().add(saleItem);

        StockMovement existingMovement = new StockMovement();
        existingMovement.setType(MovementType.SALE);
        when(stockMovementRepository.findBySaleId(10L)).thenReturn(Optional.of(existingMovement));

        stockMovementServices.applyOrCreateFromSale(sale);

        verify(inventoryService).decrease(product, 3);
        verify(stockMovementRepository).save(existingMovement);
        assertThat(existingMovement.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getProduct()).isSameAs(product);
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getStockMovement()).isSameAs(existingMovement);
        });
    }

    @Test
    void shouldRejectManualMovementWithoutItems() {
        StockMovementRequestDTO request = new StockMovementRequestDTO();
        request.setType(MovementType.ENTRY);

        assertThatThrownBy(() -> stockMovementServices.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("At least one item is required!");
    }

    @Test
    void shouldRequireReasonForAdjustment() {
        StockMovementRequestDTO request = requestWithOneItem(MovementType.ADJUSTMENT);

        assertThatThrownBy(() -> stockMovementServices.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Reason is required for adjustments!");
    }

    @Test
    void shouldRequireExitReasonForExitMovement() {
        StockMovementRequestDTO request = requestWithOneItem(MovementType.EXIT);

        assertThatThrownBy(() -> stockMovementServices.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Exit reason is required for exits!");
    }

    @Test
    void shouldRejectManualSaleMovement() {
        StockMovementRequestDTO request = requestWithOneItem(MovementType.SALE);

        assertThatThrownBy(() -> stockMovementServices.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Sale movements are generated automatically!");
    }

    private StockMovementRequestDTO requestWithOneItem(MovementType type) {
        StockMovementItemRequestDTO item = new StockMovementItemRequestDTO();
        StockMovementRequestDTO request = new StockMovementRequestDTO();
        request.setType(type);
        request.setItems(List.of(item));
        return request;
    }
}
