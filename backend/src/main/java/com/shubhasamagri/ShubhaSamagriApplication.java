package com.shubhasamagri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ShubhaSamagri - Pooja Essentials Ecommerce Application
 * Main entry point for the Spring Boot application.
 */
@SpringBootApplication
public class ShubhaSamagriApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShubhaSamagriApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  ShubhaSamagri Backend Started! 🙏");
        System.out.println("  API:     http://localhost:8080");
        System.out.println("  Swagger: http://localhost:8080/swagger-ui.html");
        System.out.println("  H2 DB:   http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}
