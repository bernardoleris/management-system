package br.com.system.services;

import br.com.system.data.dto.response.analytics.ChartResponseDTO;
import br.com.system.data.dto.response.analytics.ProductWithoutMovementResponseDTO;
import br.com.system.data.dto.response.analytics.TopProductsResponseDTO;
import br.com.system.enums.AnalyticsPeriod;
import br.com.system.enums.Payment;
import br.com.system.model.Product;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.SaleItemRepository;
import br.com.system.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@Service
public class AnalyticsService {
    private final Logger logger = Logger.getLogger(AnalyticsService.class.getName());

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // ─── Vendas por mês ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ChartResponseDTO getSalesByMonth(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting sales by month!");

        LocalDateTime[] range = resolveRange(period, start, end);

        List<Object[]> results = saleRepository.countSalesByMonth(range[0], range[1]);

        List<String> labels = results.stream()
                .map(r -> Month.of(((Number) r[0]).intValue())
                        .getDisplayName(TextStyle.FULL, new Locale("pt", "BR")))
                .toList();

        List<Number> data = results.stream()
                .map(r -> (Number) r[1])
                .toList();

        return new ChartResponseDTO(labels, data);
    }

    // ─── Faturamento por mês ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ChartResponseDTO getRevenueByMonth(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting revenue by month!");

        LocalDateTime[] range = resolveRange(period, start, end);

        List<Object[]> results = saleRepository.revenueByMonth(range[0], range[1]);

        List<String> labels = results.stream()
                .map(r -> Month.of(((Number) r[0]).intValue())
                        .getDisplayName(TextStyle.FULL, new Locale("pt", "BR")))
                .toList();

        List<Number> data = results.stream()
                .map(r -> (Number) r[1])
                .toList();

        return new ChartResponseDTO(labels, data);
    }

    // ─── Forma de pagamento ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ChartResponseDTO getPaymentMethods(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting payment methods!");

        LocalDateTime[] range = resolveRange(period, start, end);

        List<Object[]> results = saleRepository.countByPaymentMethod(range[0], range[1]);

        List<String> labels = results.stream()
                .map(r -> Payment.valueOf(r[0].toString()).getLabel())
                .toList();

        List<Number> data = results.stream()
                .map(r -> (Number) r[1])
                .toList();

        return new ChartResponseDTO(labels, data);
    }

    // ─── Produtos mais vendidos ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TopProductsResponseDTO> getTopProducts(
            AnalyticsPeriod period, LocalDate start, LocalDate end, int limit) {
        logger.info("Getting top products!");

        LocalDateTime[] range = resolveRange(period, start, end);

        return saleItemRepository.findTopProducts(range[0], range[1], PageRequest.of(0, limit))
                .stream()
                .map(r -> new TopProductsResponseDTO(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    // ─── Produtos sem giro ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductWithoutMovementResponseDTO> getProductsWithoutMovement(
            AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting products without movement!");

        LocalDateTime[] range = resolveRange(period, start, end);

        return productRepository.findProductsWithoutMovement(range[0], range[1])
                .stream()
                .map(this::toProductWithoutMovementDTO)
                .toList();
    }

    // ─── Resolução do período ─────────────────────────────────────────────────

    private LocalDateTime[] resolveRange(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return new LocalDateTime[]{
                    start.atStartOfDay(),
                    end.atTime(23, 59, 59)
            };
        }

        AnalyticsPeriod resolved = period != null ? period : AnalyticsPeriod.LAST_30_DAYS;

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = switch (resolved) {
            case LAST_7_DAYS    -> endDate.minusDays(7);
            case LAST_30_DAYS   -> endDate.minusDays(30);
            case LAST_6_MONTHS  -> endDate.minusMonths(6);
            case LAST_12_MONTHS -> endDate.minusMonths(12);
            case CURRENT_MONTH  -> endDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            case CURRENT_YEAR   -> endDate.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        };

        return new LocalDateTime[]{startDate, endDate};
    }

    // ─── Mapeamento ───────────────────────────────────────────────────────────

    private ProductWithoutMovementResponseDTO toProductWithoutMovementDTO(Product product) {
        ProductWithoutMovementResponseDTO dto = new ProductWithoutMovementResponseDTO();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setQuantity(product.getQuantity());
        dto.setDaysSinceLastSale(0L); // calculado futuramente
        return dto;
    }
}