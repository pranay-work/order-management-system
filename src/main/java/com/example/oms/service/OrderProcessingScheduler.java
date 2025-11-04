package com.example.oms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessingScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessingScheduler.class);

    private final OrderService orderService;

    public OrderProcessingScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRateString = "${order.scheduler.interval-ms:300000}")
    public void transitionPendingToProcessing() {
        int updated = orderService.processPendingOrders();
        if (updated > 0) {
            log.info("Order scheduler transitioned {} orders from PENDING to PROCESSING", updated);
        }
    }
}


