package com.example.AlertBBS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ResultController {

    @GetMapping("/")
    public String initResult() {
        // Add attributes to the model if needed
        return "testemail"; // Return the name of the view template (e.g., result.html)
    }
}