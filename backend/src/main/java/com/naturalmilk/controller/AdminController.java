package com.naturalmilk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.naturalmilk.model.AdminUser;
import com.naturalmilk.service.AdminUserService;
import com.naturalmilk.service.OrderService;
import com.naturalmilk.service.ProductService;

@Controller
public class AdminController {
    private final ProductService productService;
    private final OrderService orderService;
    private final AdminUserService adminUserService;

    public AdminController(ProductService productService, OrderService orderService, AdminUserService adminUserService) {
        this.productService = productService;
        this.orderService = orderService;
        this.adminUserService = adminUserService;
    }

    @GetMapping("/admin/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "Logged out successfully");
        }
        return "admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.getAll().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        return "admin/dashboard";
    }

    @GetMapping("/admin/settings")
    public String settings(Model model) {
        AdminUser admin = adminUserService.getPrimaryAdmin();
        model.addAttribute("username", admin != null ? admin.getUsername() : "");
        return "admin/settings";
    }

    @PostMapping("/admin/settings")
    public String updateSettings(
        @RequestParam("username") String username,
        @RequestParam(value = "password", required = false) String password,
        @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
        Model model
    ) {
        String trimmed = username == null ? "" : username.trim();
        if (trimmed.isEmpty()) {
            model.addAttribute("error", "Username is required");
            model.addAttribute("username", "");
            return "admin/settings";
        }

        if (password != null && !password.isEmpty() && !password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("username", trimmed);
            return "admin/settings";
        }

        adminUserService.updateCredentials(trimmed, password);
        model.addAttribute("message", "Admin credentials updated");
        model.addAttribute("username", trimmed);
        return "admin/settings";
    }
}
