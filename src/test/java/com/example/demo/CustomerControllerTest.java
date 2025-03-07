package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
@WithMockUser
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testId;
    private CustomerDTO testCustomerDTO;
    private CustomerRequestDTO testRequestDTO;
    private CustomerSearchDTO testSearchDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCustomerDTO = new CustomerDTO(testId, "John", "Doe", "john.doe@example.com");
        testRequestDTO = new CustomerRequestDTO("John", "Doe", "john.doe@example.com");
        testSearchDTO = new CustomerSearchDTO("John", null, null);
    }

    @Test
    void testGetCustomerById_WhenCustomerExists_ShouldReturnCustomer() throws Exception {
        // Arrange
        when(customerService.findCustomerById(testId)).thenReturn(Optional.of(testCustomerDTO));

        // Act & Assert
        mockMvc.perform(get("/api/customers/{id}", testId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService).findCustomerById(testId);
    }

    @Test
    void testGetCustomerById_WhenCustomerDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(customerService.findCustomerById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/customers/{id}", testId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());

        verify(customerService).findCustomerById(testId);
    }

    @Test
    void testCreateCustomer_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(testCustomerDTO);

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        ArgumentCaptor<CustomerDTO> customerCaptor = ArgumentCaptor.forClass(CustomerDTO.class);
        verify(customerService).createCustomer(customerCaptor.capture());
        
        CustomerDTO capturedCustomer = customerCaptor.getValue();
        assertEquals("John", capturedCustomer.getFirstName());
        assertEquals("Doe", capturedCustomer.getLastName());
        assertEquals("john.doe@example.com", capturedCustomer.getEmail());
    }

    @Test
    void testCreateCustomer_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CustomerRequestDTO invalidRequest = new CustomerRequestDTO("", "", "invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.email").exists());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void testUpdateCustomer_WithValidData_ShouldReturnOk() throws Exception {
        // Arrange
        when(customerService.updateCustomer(eq(testId), any(CustomerDTO.class))).thenReturn(testCustomerDTO);

        // Act & Assert
        mockMvc.perform(put("/api/customers/{id}", testId)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        ArgumentCaptor<CustomerDTO> customerCaptor = ArgumentCaptor.forClass(CustomerDTO.class);
        verify(customerService).updateCustomer(eq(testId), customerCaptor.capture());
        
        CustomerDTO capturedCustomer = customerCaptor.getValue();
        assertEquals("John", capturedCustomer.getFirstName());
        assertEquals("Doe", capturedCustomer.getLastName());
        assertEquals("john.doe@example.com", capturedCustomer.getEmail());
    }

    @Test
    void testUpdateCustomer_WhenCustomerDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(customerService.updateCustomer(eq(testId), any(CustomerDTO.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        // Act & Assert
        mockMvc.perform(put("/api/customers/{id}", testId)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequestDTO)))
                .andExpect(status().isNotFound());

        verify(customerService).updateCustomer(eq(testId), any(CustomerDTO.class));
    }

    @Test
    void testDeleteCustomer_WhenCustomerExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(customerService).deleteCustomer(testId);

        // Act & Assert
        mockMvc.perform(delete("/api/customers/{id}", testId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(testId);
    }

    @Test
    void testDeleteCustomer_WhenCustomerDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new CustomerNotFoundException("Customer not found")).when(customerService).deleteCustomer(testId);

        // Act & Assert
        mockMvc.perform(delete("/api/customers/{id}", testId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());

        verify(customerService).deleteCustomer(testId);
    }

    @Test
    void testSearchCustomers_WithQueryParams_ShouldReturnMatchingCustomers() throws Exception {
        // Arrange
        List<CustomerDTO> customers = Arrays.asList(testCustomerDTO);
        when(customerService.searchCustomers(any(SearchCriteria.class))).thenReturn(customers);

        // Act & Assert
        mockMvc.perform(get("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("firstName", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        ArgumentCaptor<SearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(customerService).searchCustomers(criteriaCaptor.capture());
        
        SearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertEquals("John", capturedCriteria.getFirstName());
    }

    @Test
    void testSearchCustomersPost_WithRequestBody_ShouldReturnMatchingCustomers() throws Exception {
        // Arrange
        List<CustomerDTO> customers = Arrays.asList(testCustomerDTO);
        when(customerService.searchCustomers(any(SearchCriteria.class))).thenReturn(customers);

        // Act & Assert
        mockMvc.perform(post("/api/customers/search")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSearchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        ArgumentCaptor<SearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(customerService).searchCustomers(criteriaCaptor.capture());
        
        SearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertEquals("John", capturedCriteria.getFirstName());
    }

    @Test
    void testSearchCustomers_NoResults_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(customerService.searchCustomers(any(SearchCriteria.class))).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("firstName", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(customerService).searchCustomers(any(SearchCriteria.class));
    }
}
