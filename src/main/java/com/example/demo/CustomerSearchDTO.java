package com.example.demo;

/**
 * DTO for customer search requests
 */
public class CustomerSearchDTO {
    
    private String firstName;
    private String lastName;
    private String email;
    
    // Default constructor
    public CustomerSearchDTO() {
    }
    
    // Constructor with fields
    public CustomerSearchDTO(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and Setters
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
    
    // Convert to SearchCriteria model
    public SearchCriteria toSearchCriteria() {
        return new SearchCriteria(firstName, lastName, email);
    }
}
