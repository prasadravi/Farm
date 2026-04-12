package com.naturalmilk.controller;

import com.naturalmilk.model.Order;
import com.naturalmilk.security.JwtTokenProvider;
import com.naturalmilk.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                         @RequestBody Order order) {
        try {
            String userId = extractUserId(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login to place an order.");
            }

            order.setUserId(userId);

            if (order.getDeliveryDetails() != null) {
                order.setAddress(order.getDeliveryDetails().getAddress());
                order.setLandmark(order.getDeliveryDetails().getLandmark());
                order.setPincode(order.getDeliveryDetails().getPincode());
                order.setPhone(order.getDeliveryDetails().getPhone());
            }

            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            System.err.println("Create order error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to create order: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Get order error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get order");
        }
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> getUserOrders(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            String userId = extractUserId(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login to view your orders.");
            }

            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.err.println("Get user orders error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get orders");
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        try {
            Order updated = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("Update order status error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update order status");
        }
    }
}
