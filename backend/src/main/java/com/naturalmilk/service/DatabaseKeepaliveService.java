package com.naturalmilk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.naturalmilk.repository.ProductRepository;

/**
 * Service to keep the database connection alive by executing periodic queries.
 * Prevents database connections from stalling due to inactivity timeouts.
 */
@Service
public class DatabaseKeepaliveService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseKeepaliveService.class);

    @Autowired
    private ProductRepository productRepository;

    /**
     * Executes a simple query every 3 minutes (180000 ms) to keep the database connection alive.
     * This prevents the connection from being closed due to inactivity timeouts.
     */
    @Scheduled(fixedRate = 180000, initialDelay = 30000)
    public void keepDatabaseConnectionAlive() {
        try {
            // Execute a simple count query to keep the connection alive
            long productCount = productRepository.count();
            logger.debug("Database keepalive executed successfully. Total products in database: {}", productCount);
        } catch (Exception e) {
            logger.error("Error during database keepalive ping: ", e);
        }
    }
}
