package com.example.oms.health;

import com.example.oms.service.OrderService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Custom health indicator for the Order Service.
 * Checks if the service can perform basic operations.
 */
@Component
public class OrderServiceHealthIndicator implements HealthIndicator {
    
    private static final String SERVICE_NAME = "order-service";
    private static final long TIMEOUT_MS = 5000; // 5 seconds timeout
    
    private final OrderService orderService;
    
    public OrderServiceHealthIndicator(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Override
    public Health health() {
        try {
            // Check if we can perform a simple operation within the timeout
            CompletableFuture<Boolean> healthCheck = CompletableFuture.supplyAsync(() -> {
                try {
                    // Try to list first page of orders as a health check
                    orderService.listOrders(0, 1, "createdAt", "DESC");
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
            
            boolean isHealthy = healthCheck.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            if (isHealthy) {
                return Health.up()
                    .withDetail("service", SERVICE_NAME)
                    .withDetail("message", "Service is healthy")
                    .build();
            } else {
                return Health.down()
                    .withDetail("service", SERVICE_NAME)
                    .withDetail("message", "Service check failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("service", SERVICE_NAME)
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
