package com.github.dimitryivaniuta.gateway.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the banking reporting microservice.
 */
@SpringBootApplication
public class BankingReportingApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args raw application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BankingReportingApplication.class, args);
    }
}
