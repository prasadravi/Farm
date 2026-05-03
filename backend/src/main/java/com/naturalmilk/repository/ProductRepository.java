package com.naturalmilk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naturalmilk.model.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
    List<Product> findAllByOrderByCreatedAtDesc();
}
