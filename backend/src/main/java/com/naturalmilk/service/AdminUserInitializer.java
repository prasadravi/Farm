package com.naturalmilk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements ApplicationRunner {
    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    private final AdminUserService adminUserService;

    public AdminUserInitializer(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Override
    public void run(ApplicationArguments args) {
        adminUserService.ensureDefaultAdmin(adminUsername, adminPassword);
    }
}
