package com.naturalmilk.service;

import com.naturalmilk.model.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;

@Service
public class OrderService {
    // TODO: Replace with JPA repository implementation for PostgreSQL

    public Order createOrder(Order order) {
        return order;
    }

    public Order getOrderById(String orderId) {
        return null;
    }

    public List<Order> getUserOrders(String userId) {
        return Collections.emptyList();
    }

    public Order updateOrderStatus(String orderId, String status) {
        return null;
    }
}
