package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Customer operations
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;
    
    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    /**
     * Create a new customer
     * @param requestDTO customer data to create
     * @return created customer with ID
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {
        logger.info("REST request to create a new customer");
        CustomerDTO createdCustomer = customerService.createCustomer(requestDTO.toCustomerDTO());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomerResponseDTO.fromCustomerDTO(createdCustomer));
    }
    
    /**
     * Get customer by ID
     * @param id customer UUID
     * @return customer data if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {
        logger.info("REST request to get customer with ID: {}", id);
        return customerService.findCustomerById(id)
                .map(customerDTO -> ResponseEntity.ok(CustomerResponseDTO.fromCustomerDTO(customerDTO)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update an existing customer
     * @param id customer UUID
     * @param requestDTO updated customer data
     * @return updated customer data
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequestDTO requestDTO) {
        logger.info("REST request to update customer with ID: {}", id);
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomer(id, requestDTO.toCustomerDTO());
            return ResponseEntity.ok(CustomerResponseDTO.fromCustomerDTO(updatedCustomer));
        } catch (CustomerNotFoundException e) {
            logger.error("Customer not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a customer by ID
     * @param id customer UUID
     * @return empty response with appropriate status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        logger.info("REST request to delete customer with ID: {}", id);
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (CustomerNotFoundException e) {
            logger.error("Customer not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Search for customers using query parameters
     * @param firstName optional first name filter
     * @param lastName optional last name filter
     * @param email optional email filter
     * @return list of matching customers
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> searchCustomers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email) {
        logger.info("REST request to search customers by criteria");
        CustomerSearchDTO searchDTO = new CustomerSearchDTO(firstName, lastName, email);
        List<CustomerDTO> customers = customerService.searchCustomers(searchDTO.toSearchCriteria());
        
        List<CustomerResponseDTO> responseList = customers.stream()
                .map(CustomerResponseDTO::fromCustomerDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseList);
    }
    
    /**
     * Search for customers with POST endpoint and request body
     * @param searchDTO search criteria
     * @return list of matching customers
     */
    @PostMapping("/search")
    public ResponseEntity<List<CustomerResponseDTO>> searchCustomersPost(
            @RequestBody CustomerSearchDTO searchDTO) {
        logger.info("REST request to search customers by criteria (POST)");
        List<CustomerDTO> customers = customerService.searchCustomers(searchDTO.toSearchCriteria());
        
        List<CustomerResponseDTO> responseList = customers.stream()
                .map(CustomerResponseDTO::fromCustomerDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseList);
    }
}
