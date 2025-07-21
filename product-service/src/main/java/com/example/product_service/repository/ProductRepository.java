package com.example.product_service.repository;

import com.example.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all active products with pagination
    Page<Product> findByActiveTrue(Pageable pageable);

    // Check if product exists and is active
    Optional<Product> findByIdAndActiveTrue(Long id);

    // Search products by name (case insensitive)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);
}
