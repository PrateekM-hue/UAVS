package com.company.uavs.repository;

import com.company.uavs.entity.ProductCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCatalogRepository extends JpaRepository<ProductCatalog, String> {
    
    Optional<ProductCatalog> findByProductEanAndActiveTrue(String productEan);
    
    List<ProductCatalog> findByCategoryAndActiveTrue(String category);
    
    List<ProductCatalog> findByVendorEndpointAndActiveTrue(String vendorEndpoint);
    
    @Query("SELECT p FROM ProductCatalog p WHERE p.productEan = :productEan AND p.active = true AND p.currency = :currency")
    Optional<ProductCatalog> findByProductEanAndCurrency(@Param("productEan") String productEan, 
                                                        @Param("currency") String currency);
    
    @Query("SELECT COUNT(p) FROM ProductCatalog p WHERE p.active = true")
    long countActiveProducts();
}