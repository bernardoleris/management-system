package br.com.system.controllers.api;

import br.com.system.data.dto.request.ProductRequestDTO;
import br.com.system.data.dto.response.ProductResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Product", description = "Endpoints for product management.")
public interface ProductApi {

    @Operation(summary = "List products", description = "Returns all products paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "name,asc")
    })
    Page<ProductResponseDTO> findAll(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name") Pageable pageable);

    @Operation(summary = "List active products", description = "Returns all active products paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "name,asc")
    })
    Page<ProductResponseDTO> findActive(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name") Pageable pageable);

    @Operation(summary = "List products by category", description = "Returns products for the informed category paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "name,asc")
    })
    Page<ProductResponseDTO> findByCategory(
            @PathVariable("categoryId") Long categoryId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name") Pageable pageable);

    @Operation(summary = "List products by brand", description = "Returns products for the informed brand paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "name,asc")
    })
    Page<ProductResponseDTO> findByBrand(
            @PathVariable("brandId") Long brandId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name") Pageable pageable);

    @Operation(summary = "List products by supplier", description = "Returns products for the informed supplier paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Items per page", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction", example = "name,asc")
    })
    Page<ProductResponseDTO> findBySupplier(
            @PathVariable("supplierId") Long supplierId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "name") Pageable pageable);

    @Operation(summary = "Get product by barcode", description = "Returns the product for the informed barcode.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ProductResponseDTO findByBarcode(@PathVariable("barcode") String barcode);

    @Operation(summary = "Get product by id", description = "Returns the product for the informed id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ProductResponseDTO findById(@PathVariable("id") Long id);

    @Operation(summary = "Create product", description = "Creates a new product.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Barcode already registered", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ProductResponseDTO> create(@RequestBody ProductRequestDTO product);

    @Operation(summary = "Update product", description = "Updates the product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Barcode already registered", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ProductResponseDTO update(@PathVariable("id") Long id, @RequestBody ProductRequestDTO product);

    @Operation(summary = "Delete product", description = "Deletes the product if it has no linked records.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Product has linked records", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<Void> delete(@PathVariable("id") Long id);

    @Operation(summary = "Toggle product active flag", description = "Toggles the active flag for the product.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product updated successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<Void> toggleActive(@PathVariable("id") Long id);
}
