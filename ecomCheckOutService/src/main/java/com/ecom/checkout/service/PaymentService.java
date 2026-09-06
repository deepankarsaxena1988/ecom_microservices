package com.ecom.checkout.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    private RazorpayClient client;

    @PostConstruct
    public void init() throws RazorpayException {
        client = new RazorpayClient(keyId, keySecret);
    }

    public JSONObject createOrder(String receipt, int amountInPaise, String currency) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);
        return client.Orders.create(orderRequest).toJson();
    }

    public boolean verifyWebhookSignature(String payload, String actualSignature, String secret) {
        try {
            Utils.verifyWebhookSignature(payload, actualSignature, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
