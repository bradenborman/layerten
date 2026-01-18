package com.layerten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Development server launcher for local testing.
 * Runs the application with 'devl' and 'local' profiles active.
 * 
 * To run:
 * - In IntelliJ: Right-click this file and select "Run 'DevlServer.main()'"
 * - From command line: ./gradlew :server:bootRun --args='--spring.profiles.active=devl,local'
 */
@SpringBootApplication
public class DevlServer {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DevlServer.class);
        app.setAdditionalProfiles("devl", "local");
        app.run(args);
    }
}
