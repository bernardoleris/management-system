package br.com.system.controllers.api;

import br.com.system.data.dto.request.SaleItemRequestDTO;
import br.com.system.data.dto.response.SaleItemResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Sale Item", description = "Endpoints for sale item management.")
public interface SaleItemApi {
    @Operation(summary = "List sale items", description = "Returns all items for the informed sale.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale items found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SaleItemResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    List<SaleItemResponseDTO> findBySale(@PathVariable("saleId") Long saleId);

    @Operation(summary = "Get sale item by id", description = "Returns the item for the informed sale and item id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale item found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sale item not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    SaleItemResponseDTO findById(@PathVariable("saleId") Long saleId, @PathVariable("itemId") Long itemId);

    @Operation(summary = "Create sale item", description = "Creates a new sale item.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sale item created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<SaleItemResponseDTO> create(@PathVariable("saleId") Long saleId, @RequestBody SaleItemRequestDTO item);

    @Operation(summary = "Update sale item", description = "Updates the sale item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale item updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sale item not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    SaleItemResponseDTO update(
            @PathVariable("saleId") Long saleId,
            @PathVariable("itemId") Long itemId,
            @RequestBody SaleItemRequestDTO item);

    @Operation(summary = "Delete sale item", description = "Deletes the sale item.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sale item deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sale item not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<?> delete(@PathVariable("saleId") Long saleId, @PathVariable("itemId") Long itemId);
}
