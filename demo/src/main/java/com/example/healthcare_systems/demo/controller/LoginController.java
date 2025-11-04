package com.example.healthcare_systems.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.stereotype.Controller;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login"; // returns templates/login.html
    }
}
