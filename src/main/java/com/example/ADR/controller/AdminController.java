package com.example.ADR.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final ApplicationContext context;

    public AdminController(ApplicationContext context) {
        this.context = context;
    }

    @PostMapping("/exit")
    public ResponseEntity<String> shutdown() {
        // Respond immediately
        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(500); // Optional small delay before shutdown
                System.exit(0); // Graceful Spring shutdown
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        shutdownThread.setDaemon(false);
        shutdownThread.start();

        return ResponseEntity.accepted().body("Shutdown initiated. The application will stop shortly.");
    }
}