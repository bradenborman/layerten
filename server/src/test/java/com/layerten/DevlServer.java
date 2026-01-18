package com.layerten;

import org.springframework.boot.SpringApplication;

/**
 * Development server launcher for local testing.
 * Runs the application with 'devl' and 'local' profiles active.
 * 
 * To run in Kiro:
 * - Open this file
 * - Click the "Run" code lens above the main method
 * - Or use Command Palette: "Java: Run Java File"
 * 
 * The profiles are automatically set in the code, no need to pass arguments.
 */
public class DevlServer {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LayerTenApplication.class);
        app.setAdditionalProfiles("devl", "local");
        app.run(args);
    }
}
