package com.naturalmilk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naturalmilk.model.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
