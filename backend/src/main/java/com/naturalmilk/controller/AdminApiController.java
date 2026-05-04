package com.naturalmilk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalmilk.model.Order;
import com.naturalmilk.model.Product;
import com.naturalmilk.service.OrderService;
import com.naturalmilk.service.ProductService;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {
    private final ProductService productService;
    private final OrderService orderService;

    public AdminApiController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return productService.getAll();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return productService.create(product);
    }

    @PostMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        Product existing = productService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setImageUrl(product.getImageUrl());
        existing.setQuantity(product.getQuantity());
        existing.setCategory(product.getCategory());
        existing.setQuantity250(product.getQuantity250());
        existing.setQuantity500(product.getQuantity500());
        existing.setQuantity1000(product.getQuantity1000());
        return ResponseEntity.ok(productService.update(existing));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public List<Order> listOrders() {
        return orderService.getAllOrders();
    }
}
