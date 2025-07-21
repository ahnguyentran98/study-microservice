package com.example.product_service.service;

import com.example.product_service.dto.ProductCreateDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products", key = "#id")
    public Optional<ProductResponseDTO> findById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(this::convertToResponseDTO);
    }

    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::convertToResponseDTO);
    }

    public List<ProductResponseDTO> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"products", "allProducts", "productsByCategory"}, allEntries = true)
    public ProductResponseDTO create(ProductCreateDTO createDTO) {
        Product product = convertToEntity(createDTO);
        Product savedProduct = productRepository.save(product);
        return convertToResponseDTO(savedProduct);
    }

    @CacheEvict(value = {"products", "allProducts", "productsByCategory"}, allEntries = true)
    public Optional<ProductResponseDTO> update(Long id, ProductCreateDTO updateDTO) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(existingProduct -> {
                    updateProductFromDTO(existingProduct, updateDTO);
                    Product savedProduct = productRepository.save(existingProduct);
                    return convertToResponseDTO(savedProduct);
                });
    }

    @CacheEvict(value = {"products", "allProducts", "productsByCategory"}, allEntries = true)
    public boolean deleteById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(product -> {
                    product.setActive(false); // Soft delete
                    productRepository.save(product);
                    return true;
                })
                .orElse(false);
    }

    // Helper methods
    private ProductResponseDTO convertToResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getImageUrl(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private Product convertToEntity(ProductCreateDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        return product;
    }

    private void updateProductFromDTO(Product product, ProductCreateDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
    }
}
