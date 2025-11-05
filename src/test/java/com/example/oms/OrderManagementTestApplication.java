package com.example.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.oms.repository")
@EntityScan("com.example.oms.model")
public class OrderManagementTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderManagementTestApplication.class, args);
    }
}
