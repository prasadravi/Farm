package com.naturalmilk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.naturalmilk.repository.ProductRepository;

@Component
public class DatabaseKeepAlive {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseKeepAlive.class);
    private final ProductRepository productRepository;

    public DatabaseKeepAlive(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Scheduled(
        initialDelayString = "${app.db.keepalive.initial-ms:60000}",
        fixedDelayString = "${app.db.keepalive.ms:180000}"
    )
    public void pingDatabase() {
        try {
            productRepository.count();
        } catch (Exception ex) {
            logger.debug("DB keepalive failed: {}", ex.getMessage());
        }
    }
}
