package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCustomer = new Customer();
        testCustomer.setId(testId);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
    }

    @Test
    void createCustomer_ShouldReturnSavedCustomerDTO() {
        // Arrange
        CustomerDTO inputDto = new CustomerDTO("John", "Doe", "john.doe@example.com");
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        CustomerDTO result = customerService.createCustomer(inputDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void findCustomerById_WhenExists_ShouldReturnCustomerDTO() {
        // Arrange
        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));

        // Act
        Optional<CustomerDTO> result = customerService.findCustomerById(testId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testId);
        assertThat(result.get().getFirstName()).isEqualTo("John");
        verify(customerRepository, times(1)).findById(testId);
    }

    @Test
    void findCustomerById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<CustomerDTO> result = customerService.findCustomerById(nonExistentId);

        // Assert
        assertThat(result).isEmpty();
        verify(customerRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void updateCustomer_WhenExists_ShouldReturnUpdatedCustomerDTO() {
        // Arrange
        CustomerDTO updateDto = new CustomerDTO("Jane", "Doe", "jane.doe@example.com");
        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(testId);
        updatedCustomer.setFirstName("Jane");
        updatedCustomer.setLastName("Doe");
        updatedCustomer.setEmail("jane.doe@example.com");

        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // Act
        CustomerDTO result = customerService.updateCustomer(testId, updateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        verify(customerRepository, times(1)).findById(testId);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenNotExists_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        CustomerDTO updateDto = new CustomerDTO("Jane", "Doe", "jane.doe@example.com");
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.updateCustomer(nonExistentId, updateDto);
        });
        verify(customerRepository, times(1)).findById(nonExistentId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_WhenExists_ShouldDeleteCustomer() {
        // Arrange
        when(customerRepository.existsById(testId)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(testId);

        // Act
        customerService.deleteCustomer(testId);

        // Assert
        verify(customerRepository, times(1)).existsById(testId);
        verify(customerRepository, times(1)).deleteById(testId);
    }

    @Test
    void deleteCustomer_WhenNotExists_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer(nonExistentId);
        });
        verify(customerRepository, times(1)).existsById(nonExistentId);
        verify(customerRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void searchCustomers_WithEmptyCriteria_ShouldReturnAllCustomers() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setId(UUID.randomUUID());
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");

        List<Customer> allCustomers = Arrays.asList(testCustomer, customer2);
        when(customerRepository.findAll()).thenReturn(allCustomers);

        SearchCriteria emptyCriteria = new SearchCriteria();

        // Act
        List<CustomerDTO> results = customerService.searchCustomers(emptyCriteria);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
        assertThat(results.get(1).getFirstName()).isEqualTo("Jane");
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void searchCustomers_WithFirstNameCriteria_ShouldReturnMatchingCustomers() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setId(UUID.randomUUID());
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");

        List<Customer> allCustomers = Arrays.asList(testCustomer, customer2);
        when(customerRepository.findAll()).thenReturn(allCustomers);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setFirstName("Ja");

        // Act
        List<CustomerDTO> results = customerService.searchCustomers(criteria);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Jane");
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void searchCustomers_WithMultipleCriteria_ShouldReturnMatchingCustomers() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setId(UUID.randomUUID());
        customer2.setFirstName("John");
        customer2.setLastName("Smith");
        customer2.setEmail("john.smith@example.com");

        List<Customer> allCustomers = Arrays.asList(testCustomer, customer2);
        when(customerRepository.findAll()).thenReturn(allCustomers);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setFirstName("John");
        criteria.setLastName("Doe");

        // Act
        List<CustomerDTO> results = customerService.searchCustomers(criteria);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
        assertThat(results.get(0).getLastName()).isEqualTo("Doe");
        verify(customerRepository, times(1)).findAll();
    }
}
