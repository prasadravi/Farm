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
            existing.setQuantity(form.getQuantity());
            existing.setCategory(form.getCategory());
            productService.update(existing);
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable String id) {
        productService.deleteById(id);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/out-of-stock")
    public String markOutOfStock(@PathVariable String id) {
        Product existing = productService.getById(id);
        if (existing != null) {
            existing.setQuantity(0);
            productService.update(existing);
        }
        return "redirect:/admin/products";
    }
}
