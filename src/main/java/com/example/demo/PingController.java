package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/secured-ping")
    public String securedPing() {
        return "Ping! You are authenticated.";
    }
}
