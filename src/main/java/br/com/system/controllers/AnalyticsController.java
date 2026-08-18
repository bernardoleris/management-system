package br.com.system.controllers;

import br.com.system.controllers.api.AnalyticsApi;
import br.com.system.data.dto.response.analytics.ChartResponseDTO;
import br.com.system.data.dto.response.analytics.ProductWithoutMovementResponseDTO;
import br.com.system.data.dto.response.analytics.TopProductsResponseDTO;
import br.com.system.enums.AnalyticsPeriod;
import br.com.system.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController implements AnalyticsApi {

    @Autowired
    private AnalyticsService service;

    @GetMapping("/sales-by-month")
    @Override
    public ResponseEntity<ChartResponseDTO> getSalesByMonth(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getSalesByMonth(period, start, end));
    }

    @GetMapping("/revenue-by-month")
    @Override
    public ResponseEntity<ChartResponseDTO> getRevenueByMonth(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getRevenueByMonth(period, start, end));
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

    @GetMapping("/products-without-movement")
    @Override
    public ResponseEntity<List<ProductWithoutMovementResponseDTO>> getProductsWithoutMovement(
            @RequestParam(required = false) AnalyticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getProductsWithoutMovement(period, start, end));
    }
}