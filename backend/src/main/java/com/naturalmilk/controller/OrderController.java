package com.naturalmilk.controller;

import com.naturalmilk.model.Order;
import com.naturalmilk.security.JwtTokenProvider;
import com.naturalmilk.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader("Authorization") String token, @RequestBody Order order) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
            order.setUserId(userId);

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
    public ResponseEntity<?> getUserOrders(@RequestHeader("Authorization") String token) {
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
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
