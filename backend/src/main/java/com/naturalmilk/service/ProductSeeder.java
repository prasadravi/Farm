package com.naturalmilk.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.naturalmilk.model.Product;
import com.naturalmilk.repository.ProductRepository;

@Component
public class ProductSeeder implements ApplicationRunner {
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Value("${app.frontend.base-url:https://natural-milk-frontend.onrender.com}")
    private String frontendBaseUrl;

    public ProductSeeder(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

        String base = normalizeBaseUrl(frontendBaseUrl);
        List<Product> seeds = List.of(
            createProduct("Fresh Cow Milk", 28, 50, "milk", base + "/images/stor-one.jpg"),
            createProduct("Cow Curd", 110, 40, "curd", base + "/images/cow-curd.jpg"),
            createProduct("Bufflo Curd", 155, 30, "curd", base + "/images/buffalo-curd.jpg"),
            createProduct("Bufflo Milk", 110, 50, "milk", base + "/images/store-four.jpg")
        );

        for (Product product : seeds) {
            productService.create(product);
        }
    }

    private Product createProduct(String name, double price, int quantity, String category, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setQuantity250(quantity);
        product.setQuantity500(quantity);
        product.setQuantity1000(quantity);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        return product;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
