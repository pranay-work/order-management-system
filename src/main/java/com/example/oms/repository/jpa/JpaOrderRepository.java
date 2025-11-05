package com.example.oms.repository.jpa;

import com.example.oms.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items")
    Page<OrderEntity> findAllWithItems(Pageable pageable);
    
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.status = :status")
    Page<OrderEntity> findByStatusWithItems(@Param("status") OrderStatus status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);
    
    // Keep the old methods for backward compatibility
    @Deprecated
    List<OrderEntity> findByStatus(OrderStatus status);
}

