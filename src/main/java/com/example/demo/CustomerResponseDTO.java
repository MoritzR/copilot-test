package com.example.demo;

import java.util.UUID;

/**
 * DTO for customer responses from the API
 */
public class CustomerResponseDTO {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    
    // Default constructor
    public CustomerResponseDTO() {
    }
    
    // Constructor with fields
    public CustomerResponseDTO(UUID id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Factory method to convert from CustomerDTO
    public static CustomerResponseDTO fromCustomerDTO(CustomerDTO customerDTO) {
        return new CustomerResponseDTO(
                customerDTO.getId(),
                customerDTO.getFirstName(),
                customerDTO.getLastName(),
                customerDTO.getEmail()
        );
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
