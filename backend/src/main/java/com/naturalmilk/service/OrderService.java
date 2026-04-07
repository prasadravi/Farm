package com.naturalmilk.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.naturalmilk.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {

    @Autowired
    private Firestore firestore;

    public Order createOrder(Order order) throws ExecutionException, InterruptedException {
        order.setId(UUID.randomUUID().toString());
        order.setStatus("pending");
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        firestore.collection("orders").document(order.getId()).set(order).get();
        System.out.println("Order created: " + order.getId());
        return order;
    }

    public Order getOrderById(String orderId) throws ExecutionException, InterruptedException {
        return firestore.collection("orders").document(orderId).get().get().toObject(Order.class);
    }

    public List<Order> getUserOrders(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot query = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .get();

        return query.toObjects(Order.class);
    }

    public Order updateOrderStatus(String orderId, String status) throws ExecutionException, InterruptedException {
        Order order = getOrderById(orderId);
        if (order != null) {
            order.setStatus(status);
            order.setUpdatedAt(System.currentTimeMillis());
            firestore.collection("orders").document(orderId).set(order).get();
            System.out.println("Order status updated: " + orderId + " -> " + status);
        }
        return order;
    }
}
