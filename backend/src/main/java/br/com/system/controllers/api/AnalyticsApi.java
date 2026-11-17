package br.com.system.controllers.api;

import br.com.system.data.dto.response.analytics.ChartResponseDTO;
import br.com.system.data.dto.response.ProductWithoutSalesResponseDTO;
import br.com.system.data.dto.response.analytics.TopProductsResponseDTO;
import br.com.system.enums.AnalyticsPeriod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Analytics", description = "Endpoints for business analytics and insights.")
public interface AnalyticsApi {

    @Operation(
            summary = "Sales over time",
            description = "Returns completed sales grouped by day for ranges up to 31 days and by calendar month for longer ranges. " +
                    "Missing time buckets are included with zero values. Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ChartResponseDTO> getSalesOverTime(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    @Operation(
            summary = "Revenue over time",
            description = "Returns completed-sale revenue grouped by day for ranges up to 31 days and by calendar month for longer ranges. " +
                    "Missing time buckets are included with zero values. Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ChartResponseDTO> getRevenueOverTime(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    @Operation(
            summary = "Payment methods",
            description = "Returns the count of sales grouped by payment method. " +
                    "Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ChartResponseDTO> getPaymentMethods(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    @Operation(
            summary = "Top selling products",
            description = "Returns the most sold products ranked by quantity. " +
                    "Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TopProductsResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<TopProductsResponseDTO>> getTopProducts(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @Parameter(description = "Number of products to return", example = "10")
            @RequestParam(defaultValue = "10") int limit);

    @Operation(
            summary = "Products without sales",
            description = "Returns active products without a completed sale in the informed period. " +
                    "daysSinceLastSale is calculated through the request date and is null when the product has never had a completed sale. " +
                    "Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductWithoutSalesResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<ProductWithoutSalesResponseDTO>> getProductsWithoutSales(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);
}
