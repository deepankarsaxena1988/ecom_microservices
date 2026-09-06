package com.ecom.checkout.controller;

import com.ecom.checkout.service.PaymentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CheckoutController {

    @Autowired
    private PaymentService paymentService;

    @Value("${razorpay.keyId:}")
    private String razorpayKeyId;

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Map<String, Object> body) {
        Object cart = body.get("cart");
        if (cart == null || !(cart instanceof List) || ((List) cart).isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Invalid cart");
            return ResponseEntity.badRequest().body(err);
        }

        // If client provided amount, use it. Amount expected in rupees (double/number)
        Object amountObj = body.get("amount");
        if (amountObj != null) {
            try {
                double amountRupees = Double.parseDouble(amountObj.toString());
                int amountPaise = (int) Math.round(amountRupees * 100);
                try {
                    JSONObject order = paymentService.createOrder("receipt_" + System.currentTimeMillis(), amountPaise, "INR");
                    Map<String, Object> res = new HashMap<>();
                    res.put("razorpayOrder", order.toMap());
                    res.put("razorpayKeyId", razorpayKeyId);
                    return ResponseEntity.status(201).body(res);
                } catch (Exception e) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Failed to create order");
                    err.put("details", e.getMessage());
                    return ResponseEntity.status(500).body(err);
                }
            } catch (NumberFormatException nfe) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "Invalid amount");
                return ResponseEntity.badRequest().body(err);
            }
        }

        Map<String, Object> order = new HashMap<>();
        order.put("id", System.currentTimeMillis());
        order.put("user", body.get("user"));
        order.put("items", cart);
        order.put("status", "created");

        Map<String, Object> res = new HashMap<>();
        res.put("order", order);
        return ResponseEntity.status(201).body(res);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
