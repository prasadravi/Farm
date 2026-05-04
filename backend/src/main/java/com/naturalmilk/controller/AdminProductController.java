package com.naturalmilk.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.naturalmilk.model.Product;
import com.naturalmilk.service.ProductService;

@Controller
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/admin/products")
    public String products(@RequestParam(value = "q", required = false) String query, Model model) {
        List<Product> products = productService.searchByName(query);
        model.addAttribute("products", products);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("newProduct", new Product());
        return "admin/products";
    }

    @PostMapping("/admin/products")
    public String createProduct(Product product) {
        product.setQuantity(calculateDefaultQuantity(product, product.getQuantity()));
        productService.create(product);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}")
    public String updateProduct(@PathVariable String id, Product form) {
        Product existing = productService.getById(id);
        if (existing != null) {
            existing.setName(form.getName());
            existing.setPrice(form.getPrice());
            existing.setImageUrl(form.getImageUrl());
            existing.setCategory(form.getCategory());
            existing.setQuantity250(form.getQuantity250());
            existing.setQuantity500(form.getQuantity500());
            existing.setQuantity1000(form.getQuantity1000());
            existing.setQuantity(calculateDefaultQuantity(form, existing.getQuantity()));
            productService.update(existing);
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable String id) {
        productService.deleteById(id);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/out-of-stock/{size}")
    public String markOutOfStock(@PathVariable String id, @PathVariable int size) {
        Product existing = productService.getById(id);
        if (existing != null) {
            if (size == 250) existing.setQuantity250(0);
            if (size == 500) existing.setQuantity500(0);
            if (size == 1000) existing.setQuantity1000(0);
            existing.setQuantity(calculateDefaultQuantity(existing, existing.getQuantity()));
            productService.update(existing);
        }
        return "redirect:/admin/products";
    }

    private int calculateDefaultQuantity(Product product, int fallback) {
        Integer q250 = product.getQuantity250();
        Integer q500 = product.getQuantity500();
        Integer q1000 = product.getQuantity1000();
        if (q250 == null && q500 == null && q1000 == null) {
            return fallback;
        }
        int total = 0;
        total += q250 == null ? 0 : Math.max(0, q250);
        total += q500 == null ? 0 : Math.max(0, q500);
        total += q1000 == null ? 0 : Math.max(0, q1000);
        return total;
    }
}
