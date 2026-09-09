package com.ecom.ordersummary.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecom.ordersummary.entity.OrderSummary;
import com.ecom.ordersummary.repository.OrderSummaryRepository;

@Service
public class OrderSummaryService {

    private final OrderSummaryRepository orderSummaryRepository;

    public OrderSummaryService(OrderSummaryRepository orderSummaryRepository) {
        this.orderSummaryRepository = orderSummaryRepository;
    }

    public OrderSummary addOrderSummary(OrderSummary orderSummary) {
        orderSummary.setId(null);
        applyDefaults(orderSummary);
        return orderSummaryRepository.save(orderSummary);
    }

    public OrderSummary updateOrderSummary(Long id, OrderSummary updatedOrderSummary) {
        OrderSummary existingOrderSummary = orderSummaryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order summary not found"));

        existingOrderSummary.setOrderId(updatedOrderSummary.getOrderId());
        existingOrderSummary.setUserId(updatedOrderSummary.getUserId());
        existingOrderSummary.setTotalAmount(updatedOrderSummary.getTotalAmount());
        existingOrderSummary.setCurrency(updatedOrderSummary.getCurrency());
        existingOrderSummary.setPaymentStatus(updatedOrderSummary.getPaymentStatus());
        existingOrderSummary.setOrderStatus(updatedOrderSummary.getOrderStatus());
        existingOrderSummary.setItemCount(updatedOrderSummary.getItemCount());
        existingOrderSummary.setDeliveryAddressId(updatedOrderSummary.getDeliveryAddressId());
        existingOrderSummary.setSummaryNotes(updatedOrderSummary.getSummaryNotes());

        applyDefaults(existingOrderSummary);
        return orderSummaryRepository.save(existingOrderSummary);
    }

    public OrderSummary getOrderSummaryById(Long id) {
        return orderSummaryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order summary not found"));
    }

    public OrderSummary getOrderSummaryByOrderId(Long orderId) {
        return orderSummaryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order summary not found"));
    }

    public List<OrderSummary> getOrderSummariesByUserId(Long userId) {
        return orderSummaryRepository.findByUserId(userId);
    }

    public List<OrderSummary> getAllOrderSummaries() {
        return orderSummaryRepository.findAll();
    }

    private void applyDefaults(OrderSummary orderSummary) {
        if (orderSummary.getCurrency() == null || orderSummary.getCurrency().isBlank()) {
            orderSummary.setCurrency("INR");
        }
        if (orderSummary.getItemCount() == null) {
            orderSummary.setItemCount(0);
        }
    }
}
