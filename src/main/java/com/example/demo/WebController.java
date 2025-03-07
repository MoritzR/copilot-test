package com.example.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    public String showCustomerForm(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/oauth2/authorization/github";
        }
        
        String githubUsername = principal.getAttribute("login");
        customerService.findCustomerByGithubUsername(githubUsername)
                .ifPresentOrElse(
                        customer -> model.addAttribute("customerForm", new CustomerRequestDTO(
                                customer.getFirstName(),
                                customer.getLastName(),
                                customer.getEmail())),
                        () -> model.addAttribute("customerForm", new CustomerRequestDTO())
                );
        return "index";
    }
    
    @PostMapping("/")
    public String createCustomer(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @ModelAttribute("customerForm") CustomerRequestDTO customerForm,
            BindingResult bindingResult,
            Model model) {
        if (principal == null) {
            return "redirect:/oauth2/authorization/github";
        }
            
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        try {
                            },
                            () -> {
                                customerService.findOrCreateCustomerForGithubUser(githubUsername, customerDTO);
                                model.addAttribute("message", "Customer created successfully!");
                            }
                    );
            
            model.addAttribute("customerForm", customerForm);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to save customer: " + e.getMessage());
        }
        return "index";
    }
}