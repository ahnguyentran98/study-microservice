package com.microservices.productservice.service;

import com.microservices.productservice.dto.ProductRequest;
import com.microservices.productservice.model.Product;
import com.microservices.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Cacheable(value = "products")
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Page<Product> getProductsWithFilters(String category, BigDecimal minPrice, 
                                               BigDecimal maxPrice, String keyword, 
                                               Pageable pageable) {
        return productRepository.findProductsWithFilters(category, minPrice, maxPrice, keyword, pageable);
    }

    @Cacheable(value = "productSearch", key = "#keyword")
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Cacheable(value = "categories")
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    @Cacheable(value = "brands")
    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    @CacheEvict(value = {"products", "categories", "brands"}, allEntries = true)
    public Product createProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setCategory(productRequest.getCategory());
        product.setBrand(productRequest.getBrand());
        product.setImageUrl(productRequest.getImageUrl());
        
        return productRepository.save(product);
    }

    @CacheEvict(value = {"products", "product", "categories", "brands"}, allEntries = true)
    public Product updateProduct(Long id, ProductRequest productRequest) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());
            product.setStockQuantity(productRequest.getStockQuantity());
            product.setCategory(productRequest.getCategory());
            product.setBrand(productRequest.getBrand());
            product.setImageUrl(productRequest.getImageUrl());
            
            return productRepository.save(product);
        }
        return null;
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public boolean updateStock(Long productId, Integer quantity) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (product.getStockQuantity() >= quantity) {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
                return true;
            }
        }
        return false;
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setIsActive(false);
            productRepository.save(product);
        }
    }

    public boolean isProductAvailable(Long productId, Integer quantity) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.map(product -> 
            product.getIsActive() && product.getStockQuantity() >= quantity)
            .orElse(false);
    }
}