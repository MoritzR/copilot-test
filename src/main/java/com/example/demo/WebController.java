package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

@Controller
public class WebController {
    
    private final CustomerService customerService;
    
    public WebController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    @GetMapping("/")
    public String showCustomerForm(Model model) {
        model.addAttribute("customerForm", new CustomerRequestDTO());
        return "index";
    }
    
    @PostMapping("/")
    public String createCustomer(@Valid @ModelAttribute("customerForm") CustomerRequestDTO customerForm, 
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        try {
            customerService.createCustomer(customerForm.toCustomerDTO());
            model.addAttribute("message", "Customer created successfully!");
            model.addAttribute("customerForm", new CustomerRequestDTO());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create customer: " + e.getMessage());
        }
        return "index";
    }
}