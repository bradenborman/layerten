package com.layerten;

import org.springframework.boot.SpringApplication;

/**
 * Development server launcher for local testing.
 * Runs the application with 'devl' and 'local' profiles active.
 * 
 * To run:
 * - In IntelliJ: Right-click this file and select "Run 'DevlServer.main()'"
 * - The profiles are automatically set in the code, no need to pass arguments
 */
public class DevlServer {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LayerTenApplication.class);
        app.setAdditionalProfiles("devl", "local");
        app.run(args);
    }
}
