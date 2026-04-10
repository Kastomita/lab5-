package ru.mfa.photoprinting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
               <h1>Photo Printing Service</h1>
               <p>Server is running!</p>
               <ul>
                   <li><a href="/swagger-ui.html">API Documentation</a></li>
                   <li><a href="/api/auth/me">My Profile</a></li>
               </ul>
               <hr>
               <h3>API Endpoints:</h3>
               <ul>
                   <li>POST /api/auth/register - Register new user</li>
                   <li>POST /api/auth/login - Login (returns JWT tokens)</li>
                   <li>POST /api/auth/refresh - Refresh access token</li>
                   <li>POST /api/auth/logout - Logout</li>
                   <li>GET /api/customers - Get all customers (USER/ADMIN)</li>
                   <li>GET /api/formats - Get all formats (USER/ADMIN)</li>
                   <li>POST /api/orders - Create order (USER/ADMIN)</li>
               </ul>
               """;
    }

    @GetMapping("/api/test")
    public String test() {
        return "Test endpoint works!";
    }
}