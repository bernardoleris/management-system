package br.com.system.controllers.api;

import br.com.system.data.dto.response.analytics.ChartResponseDTO;
import br.com.system.data.dto.response.analytics.ProductWithoutMovementResponseDTO;
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
            summary = "Sales by month",
            description = "Returns the number of completed sales grouped by month. " +
                    "Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ChartResponseDTO> getSalesByMonth(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    @Operation(
            summary = "Revenue by month",
            description = "Returns the total revenue grouped by month. " +
                    "Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ChartResponseDTO> getRevenueByMonth(
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
            summary = "Products without movement",
            description = "Returns active products that had no sales in the informed period. " +
                    "Use 'period' for predefined ranges or 'start'/'end' for custom dates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductWithoutMovementResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<ProductWithoutMovementResponseDTO>> getProductsWithoutMovement(
            @Parameter(description = "Predefined period", example = "LAST_30_DAYS")
            @RequestParam(required = false) AnalyticsPeriod period,
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);
}