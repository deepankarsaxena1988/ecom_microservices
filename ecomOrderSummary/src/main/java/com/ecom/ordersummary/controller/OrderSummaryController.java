package com.ecom.ordersummary.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ordersummary.entity.OrderSummary;
import com.ecom.ordersummary.service.OrderSummaryService;

@RestController
@RequestMapping("/order-summaries")
public class OrderSummaryController {

    private final OrderSummaryService orderSummaryService;

    public OrderSummaryController(OrderSummaryService orderSummaryService) {
        this.orderSummaryService = orderSummaryService;
    }

    @PostMapping
    public OrderSummary addOrderSummary(@RequestBody OrderSummary orderSummary) {
        return orderSummaryService.addOrderSummary(orderSummary);
    }

    @PutMapping("/{id}")
    public OrderSummary updateOrderSummary(@PathVariable Long id, @RequestBody OrderSummary orderSummary) {
        return orderSummaryService.updateOrderSummary(id, orderSummary);
    }

    @GetMapping("/{id}")
    public OrderSummary getOrderSummary(@PathVariable Long id) {
        return orderSummaryService.getOrderSummaryById(id);
    }

    @GetMapping
    public List<OrderSummary> getOrderSummaries(
        @RequestParam(name = "userId", required = false) Long userId,
        @RequestParam(name = "orderId", required = false) Long orderId) {

        if (orderId != null) {
            return List.of(orderSummaryService.getOrderSummaryByOrderId(orderId));
        }
        if (userId != null) {
            return orderSummaryService.getOrderSummariesByUserId(userId);
        }
        return orderSummaryService.getAllOrderSummaries();
    }
}
