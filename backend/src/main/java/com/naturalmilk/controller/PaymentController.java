package com.naturalmilk.controller;

// import com.google.cloud.firestore.Firestore; // Removed Firestore import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalmilk.model.payment.CreatePaymentOrderRequest;
import com.naturalmilk.model.payment.VerifyPaymentRequest;
import com.naturalmilk.security.JwtTokenProvider;
import com.naturalmilk.service.RazorpayService;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RazorpayService razorpayService;



    private String extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty() || !jwtTokenProvider.validateToken(token)) {
            return null;
        }

        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createPaymentOrder(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody CreatePaymentOrderRequest request
    ) {
        try {
            String userId = extractUserId(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please login to continue payment."));
            }

            List<?> items = request.getItems();
            if (items == null || items.isEmpty() || request.getTotal() == null || request.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid payment request."));
            }

            if (!razorpayService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("message", "Payment service is not configured."));
            }

            long amountInPaise = Math.round(request.getTotal() * 100);
            String paymentRecordId = UUID.randomUUID().toString();
            String receipt = "nm_" + System.currentTimeMillis();
            JSONObject order = razorpayService.createOrder(amountInPaise, receipt, paymentRecordId);



            Map<String, Object> response = new HashMap<>();
            response.put("paymentRecordId", paymentRecordId);
            response.put("orderId", order.getString("id"));
            response.put("amount", order.getLong("amount"));
            response.put("currency", order.getString("currency"));
            response.put("keyId", razorpayService.getKeyId());

            return ResponseEntity.ok(response);
        } catch (RazorpayException | IllegalStateException e) {
            System.err.println("Create payment order error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to create payment order."));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody VerifyPaymentRequest request
    ) {
        try {
            String userId = extractUserId(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please login to verify payment."));
            }

            boolean verified = razorpayService.verifySignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            if (!verified) {
                return ResponseEntity.badRequest().body(Map.of("message", "Payment verification failed."));
            }

            // Firestore update removed. Implement DB update here if needed.

            return ResponseEntity.ok(Map.of("verified", true, "message", "Payment verified"));
        } catch (RuntimeException e) {
            System.err.println("Verify payment error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to verify payment."));
        }
    }
}
