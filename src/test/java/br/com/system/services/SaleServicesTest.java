package br.com.system.services;

import br.com.system.data.dto.request.SaleItemRequestDTO;
import br.com.system.data.dto.request.SaleRequestDTO;
import br.com.system.enums.Payment;
import br.com.system.enums.SaleStatus;
import br.com.system.exception.BusinessException;
import br.com.system.model.Administrator;
import br.com.system.model.Product;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.SaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServicesTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementServices stockMovementServices;

    @InjectMocks
    private SaleServices saleServices;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectItemDiscountGreaterThanItsGrossValueBeforeSavingSale() {
        Administrator administrator = new Administrator();
        Product product = new Product();
        product.setSalePrice(new BigDecimal("20.00"));

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("admin", null));
        when(administratorRepository.findByLogin("admin")).thenReturn(Optional.of(administrator));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductId(1L);
        item.setQuantity(1);
        item.setDiscount(new BigDecimal("21.00"));

        SaleRequestDTO request = new SaleRequestDTO();
        request.setPaymentMethod(Payment.PIX);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> saleServices.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Item discount cannot exceed its gross value!");

        verify(saleRepository, never()).save(any());
    }

    @Test
    void shouldRejectSaleDiscountGreaterThanItemsTotalBeforeSavingSale() {
        Administrator administrator = new Administrator();
        Product product = new Product();
        product.setSalePrice(new BigDecimal("20.00"));

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("admin", null));
        when(administratorRepository.findByLogin("admin")).thenReturn(Optional.of(administrator));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductId(1L);
        item.setQuantity(1);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setPaymentMethod(Payment.PIX);
        request.setDiscount(new BigDecimal("21.00"));
        request.setItems(List.of(item));

        assertThatThrownBy(() -> saleServices.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Sale discount cannot exceed its items total!");

        verify(saleRepository, never()).save(any());
    }

    @Test
    void shouldApplyStockMovementWhenCreatingCompletedSale() {
        prepareAuthenticatedAdministrator();
        Product product = productWithPrice();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SaleRequestDTO request = saleRequest(SaleStatus.COMPLETED, 2);
        saleServices.create(request);

        org.mockito.ArgumentCaptor<br.com.system.model.Sale> saleCaptor =
                org.mockito.ArgumentCaptor.forClass(br.com.system.model.Sale.class);
        verify(stockMovementServices).applyOrCreateFromSale(saleCaptor.capture());
        assertThat(saleCaptor.getValue().getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(saleCaptor.getValue().getItems()).singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2));
    }

    @Test
    void shouldNotApplyStockMovementWhenCreatingPendingSale() {
        prepareAuthenticatedAdministrator();
        when(productRepository.findById(1L)).thenReturn(Optional.of(productWithPrice()));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        saleServices.create(saleRequest(SaleStatus.PENDING, 2));

        verify(stockMovementServices, never()).applyOrCreateFromSale(any());
    }

    @Test
    void shouldRestoreStockAndRemoveMovementWhenCancelingCompletedSale() {
        br.com.system.model.Sale sale = new br.com.system.model.Sale();
        sale.setId(10L);
        sale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);

        saleServices.cancel(10L);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELED);
        verify(stockMovementServices).reverseFromSale(sale);
        verify(stockMovementServices).removeFromSale(sale);
        verify(saleRepository).save(sale);
    }

    @Test
    void shouldRestoreStockAndRemoveMovementWhenDeletingCompletedSale() {
        br.com.system.model.Sale sale = new br.com.system.model.Sale();
        sale.setId(10L);
        sale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));

        saleServices.delete(10L);

        verify(stockMovementServices).reverseFromSale(sale);
        verify(stockMovementServices).removeFromSale(sale);
        verify(saleRepository).delete(sale);
    }

    private void prepareAuthenticatedAdministrator() {
        Administrator administrator = new Administrator();
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("admin", null));
        when(administratorRepository.findByLogin("admin")).thenReturn(Optional.of(administrator));
    }

    private Product productWithPrice() {
        Product product = new Product();
        product.setSalePrice(new BigDecimal("20.00"));
        return product;
    }

    private SaleRequestDTO saleRequest(SaleStatus status, int quantity) {
        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductId(1L);
        item.setQuantity(quantity);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setStatus(status);
        request.setPaymentMethod(Payment.PIX);
        request.setItems(List.of(item));
        return request;
    }
}
