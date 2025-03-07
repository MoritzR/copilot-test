package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for Customer management operations
 */
@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new customer from DTO
     * @param customerDTO data transfer object with customer details
     * @return CustomerDTO with generated ID
     */
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        logger.info("Creating new customer with email: {}", customerDTO.getEmail());
        
        Customer customer = new Customer();
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        
        customer = customerRepository.save(customer);
        logger.debug("Created customer with ID: {}", customer.getId());
        
        return convertToDTO(customer);
    }

    /**
     * Find customer by ID
     * @param id customer UUID
     * @return Optional containing CustomerDTO if found
     */
    public Optional<CustomerDTO> findCustomerById(UUID id) {
        logger.debug("Finding customer with ID: {}", id);
        return customerRepository.findById(id).map(this::convertToDTO);
    }

    /**
     * Update existing customer details
     * @param id customer UUID
     * @param customerDTO updated customer details
     * @return updated CustomerDTO
     * @throws CustomerNotFoundException if customer doesn't exist
     */
    public CustomerDTO updateCustomer(UUID id, CustomerDTO customerDTO) {
        logger.info("Updating customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Failed to update customer: ID {} not found", id);
                    return new CustomerNotFoundException("Customer not found with ID: " + id);
                });
        
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        
        customer = customerRepository.save(customer);
        logger.debug("Updated customer with ID: {}", id);
        
        return convertToDTO(customer);
    }

    /**
     * Delete customer by ID
     * @param id customer UUID
     * @throws CustomerNotFoundException if customer doesn't exist
     */
    public void deleteCustomer(UUID id) {
        logger.info("Deleting customer with ID: {}", id);
        
        if (!customerRepository.existsById(id)) {
            logger.error("Failed to delete customer: ID {} not found", id);
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }
        
        customerRepository.deleteById(id);
        logger.debug("Deleted customer with ID: {}", id);
    }

    /**
     * Search customers based on criteria
     * @param criteria search parameters
     * @return list of matching CustomerDTOs
     */
    public List<CustomerDTO> searchCustomers(SearchCriteria criteria) {
        logger.debug("Searching customers with criteria: firstName={}, lastName={}, email={}",
                criteria.getFirstName(), criteria.getLastName(), criteria.getEmail());
        
        if (criteria.isEmpty()) {
            logger.debug("Empty search criteria, returning all customers");
            return customerRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        
        List<Customer> results = new ArrayList<>();
        List<Customer> allCustomers = customerRepository.findAll();
        
        for (Customer customer : allCustomers) {
            boolean match = true;
            
            if (criteria.getFirstName() != null && !criteria.getFirstName().isEmpty()) {
                match = match && customer.getFirstName().toLowerCase().contains(criteria.getFirstName().toLowerCase());
            }
            
            if (match && criteria.getLastName() != null && !criteria.getLastName().isEmpty()) {
                match = match && customer.getLastName().toLowerCase().contains(criteria.getLastName().toLowerCase());
            }
            
            if (match && criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
                match = match && customer.getEmail().toLowerCase().contains(criteria.getEmail().toLowerCase());
            }
            
            if (match) {
                results.add(customer);
            }
        }
        
        logger.debug("Found {} customers matching search criteria", results.size());
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Find or create a customer for a GitHub user
     * @param githubUsername GitHub username
     * @param customerDTO initial customer data (if creating)
     * @return CustomerDTO for the existing or new customer
     */
    public CustomerDTO findOrCreateCustomerForGithubUser(String githubUsername, CustomerDTO customerDTO) {
        Optional<Customer> existingCustomer = customerRepository.findByGithubUsername(githubUsername);
        
        if (existingCustomer.isPresent()) {
            return convertToDTO(existingCustomer.get());
        }
        
        Customer newCustomer = new Customer();
        newCustomer.setFirstName(customerDTO.getFirstName());
        newCustomer.setLastName(customerDTO.getLastName());
        newCustomer.setEmail(customerDTO.getEmail());
        newCustomer.setGithubUsername(githubUsername);
        
        newCustomer = customerRepository.save(newCustomer);
        return convertToDTO(newCustomer);
    }

    /**
     * Get customer by GitHub username
     * @param githubUsername GitHub username
     * @return Optional containing CustomerDTO if found
     */
    public Optional<CustomerDTO> findCustomerByGithubUsername(String githubUsername) {
        return customerRepository.findByGithubUsername(githubUsername)
                .map(this::convertToDTO);
    }

    /**
     * Update customer for GitHub user
     * @param githubUsername GitHub username
     * @param customerDTO updated customer details
     * @return updated CustomerDTO
     * @throws CustomerNotFoundException if customer doesn't exist
     */
    public CustomerDTO updateCustomerForGithubUser(String githubUsername, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findByGithubUsername(githubUsername)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for GitHub user: " + githubUsername));
        
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        
        customer = customerRepository.save(customer);
        return convertToDTO(customer);
    }

    /**
     * Helper method to convert Customer entity to CustomerDTO
     */
    private CustomerDTO convertToDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail()
        );
    }
}
