package com.example.product_service.controller;

import com.example.product_service.dto.ProductCreateDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Service", description = "Product management operations")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all active products with optional pagination")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponseDTO> productsPage = productService.findAll(pageable);
        return ResponseEntity.ok(productsPage.getContent());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new product", description = "Create a new product in the catalog")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductCreateDTO createDTO) {
        ProductResponseDTO createdProduct = productService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductCreateDTO updateDTO) {
        return productService.update(id, updateDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Soft delete a product (mark as inactive)")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        boolean deleted = productService.deleteById(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(
            @Parameter(description = "Search criteria") @RequestBody Map<String, String> searchRequest) {
        String query = searchRequest.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductResponseDTO> products = productService.searchByName(query);
        return ResponseEntity.ok(products);
    }
}
