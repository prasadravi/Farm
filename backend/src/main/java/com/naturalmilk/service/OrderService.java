package com.naturalmilk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.naturalmilk.model.Order;
import com.naturalmilk.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Order order) {
        order.setStatus(order.getStatus() == null ? "pending" : order.getStatus());
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return null;
        }
        order.setStatus(status);
        order.setUpdatedAt(System.currentTimeMillis());
        return orderRepository.save(order);
    }
}
