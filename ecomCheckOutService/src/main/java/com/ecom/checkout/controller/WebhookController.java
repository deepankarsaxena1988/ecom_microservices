package com.ecom.checkout.controller;

import com.ecom.checkout.repo.PaymentRepository;
import com.ecom.checkout.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${razorpay.keySecret:}")
    private String razorpayKeySecret;

    @PostMapping("/razorpay")
    public ResponseEntity<?> handleRazorpayWebhook(HttpServletRequest request, @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String payload = sb.toString();

            boolean valid = paymentService.verifyWebhookSignature(payload, signature, razorpayKeySecret );
            if (!valid) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
            }

            // TODO: parse payload JSON and update Payment entity/status accordingly
            // For now, return 200
            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
