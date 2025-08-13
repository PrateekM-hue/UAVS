package com.company.uavs.repository;

import com.company.uavs.entity.SaleActivation;
import com.company.uavs.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleActivationRepository extends JpaRepository<SaleActivation, Long> {
    
    Optional<SaleActivation> findByUniqueRef(String uniqueRef);
    
    List<SaleActivation> findByInvoiceNoAndProductEan(String invoiceNo, String productEan);
    
    List<SaleActivation> findByStatusAndUpdatedAtBefore(TransactionStatus status, Instant before);
    
    List<SaleActivation> findByStatus(TransactionStatus status);
    
    @Query("SELECT s FROM SaleActivation s WHERE s.storeCode = :storeCode AND s.txnDate BETWEEN :startDate AND :endDate")
    List<SaleActivation> findByStoreCodeAndDateRange(@Param("storeCode") String storeCode,
                                                     @Param("startDate") Instant startDate,
                                                     @Param("endDate") Instant endDate);
    
    @Query("SELECT s FROM SaleActivation s WHERE s.status = 'PENDING' AND s.retryCount < :maxRetries AND s.updatedAt < :cutoffTime")
    List<SaleActivation> findPendingTransactionsForRetry(@Param("maxRetries") Integer maxRetries,
                                                         @Param("cutoffTime") Instant cutoffTime);
    
    @Query("SELECT COUNT(s) FROM SaleActivation s WHERE s.status = :status AND s.txnDate >= :since")
    long countByStatusSince(@Param("status") TransactionStatus status, @Param("since") Instant since);
    
    boolean existsByUniqueRef(String uniqueRef);
}