package com.naturalmilk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.naturalmilk.model.Product;
import com.naturalmilk.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Product product) {
        long now = System.currentTimeMillis();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return productRepository.save(product);
    }

    public Product update(Product product) {
        product.setUpdatedAt(System.currentTimeMillis());
        return productRepository.save(product);
    }

    public void deleteById(String id) {
        productRepository.deleteById(id);
    }

    public Product getById(String id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAll() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Product> searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }
        return productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(query.trim());
    }
}
