package com.layerten.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to serve the React SPA for all non-API routes.
 * This allows React Router to handle client-side routing.
 */
@Controller
public class SpaController {
    
    /**
     * Forward all non-API requests to index.html so React Router can handle routing.
     */
    @GetMapping(value = {
        "/",
        "/lists",
        "/lists/**",
        "/posts",
        "/posts/**",
        "/suggest",
        "/admin/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
