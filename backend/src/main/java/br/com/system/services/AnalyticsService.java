package br.com.system.services;

import br.com.system.data.dto.response.ProductWithoutSalesResponseDTO;
import br.com.system.data.dto.response.analytics.ChartResponseDTO;
import br.com.system.data.dto.response.analytics.TopProductsResponseDTO;
import br.com.system.enums.AnalyticsPeriod;
import br.com.system.enums.Payment;
import br.com.system.exception.BusinessException;
import br.com.system.model.Product;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.SaleItemRepository;
import br.com.system.repository.SaleRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class AnalyticsService {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DAILY_LABEL = DateTimeFormatter.ofPattern("dd/MM", PT_BR);
    private static final DateTimeFormatter DAILY_LABEL_WITH_YEAR = DateTimeFormatter.ofPattern("dd/MM/yy", PT_BR);
    private static final DateTimeFormatter MONTHLY_LABEL = DateTimeFormatter.ofPattern("MMM/yy", PT_BR);

    private final Logger logger = Logger.getLogger(AnalyticsService.class.getName());
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CurrentCompany currentCompany;

    public AnalyticsService(SaleRepository saleRepository, SaleItemRepository saleItemRepository,
                            ProductRepository productRepository, CurrentCompany currentCompany) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
        this.currentCompany = currentCompany;
    }

    @Transactional(readOnly = true)
    public ChartResponseDTO getSalesOverTime(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting sales over time!");
        LocalDateTime[] range = resolveRange(period, start, end);
        LocalDate firstDay = range[0].toLocalDate();
        LocalDate lastDay = range[1].toLocalDate();
        if (useDailyBuckets(period, start, end)) {
            return dailyChart(saleRepository.countSalesByDay(currentCompany.getId(), range[0], range[1]), firstDay, lastDay, 0L);
        }
        return monthlyChart(saleRepository.countSalesByMonth(currentCompany.getId(), range[0], range[1]), firstDay, lastDay, 0L);
    }

    @Transactional(readOnly = true)
    public ChartResponseDTO getRevenueOverTime(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting revenue over time!");
        LocalDateTime[] range = resolveRange(period, start, end);
        LocalDate firstDay = range[0].toLocalDate();
        LocalDate lastDay = range[1].toLocalDate();
        if (useDailyBuckets(period, start, end)) {
            return dailyChart(saleRepository.revenueByDay(currentCompany.getId(), range[0], range[1]), firstDay, lastDay, BigDecimal.ZERO);
        }
        return monthlyChart(saleRepository.revenueByMonth(currentCompany.getId(), range[0], range[1]), firstDay, lastDay, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public ChartResponseDTO getPaymentMethods(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting payment methods!");
        LocalDateTime[] range = resolveRange(period, start, end);
        List<Object[]> results = saleRepository.countByPaymentMethod(currentCompany.getId(), range[0], range[1]);

        List<String> labels = results.stream()
                .map(row -> Payment.valueOf(row[0].toString()).getLabel())
                .toList();
        List<Number> data = results.stream()
                .map(row -> (Number) row[1])
                .toList();
        return new ChartResponseDTO(labels, data);
    }

    @Transactional(readOnly = true)
    public List<TopProductsResponseDTO> getTopProducts(
            AnalyticsPeriod period, LocalDate start, LocalDate end, int limit) {
        logger.info("Getting top products!");
        LocalDateTime[] range = resolveRange(period, start, end);
        return saleItemRepository.findTopProducts(currentCompany.getId(), range[0], range[1], PageRequest.of(0, limit))
                .stream()
                .map(row -> new TopProductsResponseDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductWithoutSalesResponseDTO> getProductsWithoutSales(
            AnalyticsPeriod period, LocalDate start, LocalDate end) {
        logger.info("Getting products without sales!");
        LocalDateTime[] range = resolveRange(period, start, end);
        Long companyId = currentCompany.getId();
        List<Product> products = productRepository.findActiveProductsWithoutCompletedSales(companyId, range[0], range[1]);
        if (products.isEmpty()) return List.of();

        LocalDateTime asOf = LocalDateTime.now();
        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, LocalDateTime> lastSaleByProduct = new HashMap<>();
        saleItemRepository.findLastCompletedSaleDates(companyId, productIds, asOf)
                .forEach(row -> lastSaleByProduct.put(((Number) row[0]).longValue(), (LocalDateTime) row[1]));

        return products.stream().map(product -> {
            ProductWithoutSalesResponseDTO dto = new ProductWithoutSalesResponseDTO();
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setQuantity(product.getQuantity());
            LocalDateTime lastSale = lastSaleByProduct.get(product.getId());
            dto.setDaysSinceLastSale(lastSale == null ? null :
                    Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(lastSale.toLocalDate(), asOf.toLocalDate())));
            return dto;
        }).toList();
    }

    private ChartResponseDTO dailyChart(List<Object[]> rows, LocalDate start, LocalDate end, Number zeroValue) {
        Map<LocalDate, Number> valuesByDate = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = LocalDate.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue(), ((Number) row[2]).intValue());
            valuesByDate.put(date, (Number) row[3]);
        }

        DateTimeFormatter labelFormatter = start.getYear() == end.getYear() ? DAILY_LABEL : DAILY_LABEL_WITH_YEAR;
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            labels.add(date.format(labelFormatter));
            data.add(valuesByDate.getOrDefault(date, zeroValue));
        }
        return new ChartResponseDTO(labels, data);
    }

    private ChartResponseDTO monthlyChart(List<Object[]> rows, LocalDate start, LocalDate end, Number zeroValue) {
        Map<YearMonth, Number> valuesByMonth = new HashMap<>();
        for (Object[] row : rows) {
            YearMonth month = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            valuesByMonth.put(month, (Number) row[2]);
        }

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        YearMonth lastMonth = YearMonth.from(end);
        for (YearMonth month = YearMonth.from(start); !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            labels.add(month.atDay(1).format(MONTHLY_LABEL));
            data.add(valuesByMonth.getOrDefault(month, zeroValue));
        }
        return new ChartResponseDTO(labels, data);
    }

    private boolean useDailyBuckets(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        if (start != null || end != null) {
            return !start.plusDays(30).isBefore(end);
        }
        AnalyticsPeriod resolved = period == null ? AnalyticsPeriod.LAST_30_DAYS : period;
        return resolved == AnalyticsPeriod.LAST_7_DAYS || resolved == AnalyticsPeriod.LAST_30_DAYS;
    }

    private LocalDateTime[] resolveRange(AnalyticsPeriod period, LocalDate start, LocalDate end) {
        if (start != null || end != null) {
            if (start == null || end == null) {
                throw new BusinessException("Both start and end dates are required for a custom range!");
            }
            if (start.isAfter(end)) {
                throw new BusinessException("Start date must not be after end date!");
            }
            return new LocalDateTime[]{start.atStartOfDay(), end.atTime(23, 59, 59)};
        }

        LocalDateTime endDate = LocalDateTime.now();
        LocalDate today = endDate.toLocalDate();
        AnalyticsPeriod resolved = period == null ? AnalyticsPeriod.LAST_30_DAYS : period;
        LocalDate startDate = switch (resolved) {
            case LAST_7_DAYS -> today.minusDays(6);
            case LAST_30_DAYS -> today.minusDays(29);
            case LAST_6_MONTHS -> YearMonth.from(today).minusMonths(5).atDay(1);
            case LAST_12_MONTHS -> YearMonth.from(today).minusMonths(11).atDay(1);
            case CURRENT_MONTH -> today.withDayOfMonth(1);
            case CURRENT_YEAR -> today.withDayOfYear(1);
        };
        return new LocalDateTime[]{startDate.atStartOfDay(), endDate};
    }
}
