package br.com.system.controllers;

import br.com.system.controllers.api.AnalyticsApi;
import br.com.system.data.dto.response.analytics.ChartResponseDTO;
import br.com.system.data.dto.response.ProductWithoutSalesResponseDTO;
import br.com.system.data.dto.response.analytics.TopProductsResponseDTO;
import br.com.system.enums.AnalyticsPeriod;
import br.com.system.services.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController implements AnalyticsApi {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @GetMapping({"/sales-over-time", "/sales-by-month"})
    @Override
    public ResponseEntity<ChartResponseDTO> getSalesOverTime(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getSalesOverTime(period, start, end));
    }

    @GetMapping({"/revenue-over-time", "/revenue-by-month"})
    @Override
    public ResponseEntity<ChartResponseDTO> getRevenueOverTime(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getRevenueOverTime(period, start, end));
    }

    @GetMapping("/payment-methods")
    @Override
    public ResponseEntity<ChartResponseDTO> getPaymentMethods(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getPaymentMethods(period, start, end));
    }

    @GetMapping("/top-products")
    @Override
    public ResponseEntity<List<TopProductsResponseDTO>> getTopProducts(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getTopProducts(period, start, end, limit));
    }

    @GetMapping({"/products-without-sales", "/products-without-movement"})
    @Override
    public ResponseEntity<List<ProductWithoutSalesResponseDTO>> getProductsWithoutSales(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getProductsWithoutSales(period, start, end));
    }
}
