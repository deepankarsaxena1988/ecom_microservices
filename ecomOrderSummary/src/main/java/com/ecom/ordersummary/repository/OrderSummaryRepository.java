package com.ecom.ordersummary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.ordersummary.entity.OrderSummary;

@Repository
public interface OrderSummaryRepository extends JpaRepository<OrderSummary, Long> {

    Optional<OrderSummary> findByOrderId(Long orderId);

    List<OrderSummary> findByUserId(Long userId);
}
