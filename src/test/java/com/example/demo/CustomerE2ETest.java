package com.example.demo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CustomerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Test
    @WithMockUser(username = "testuser")
    void whenNewUserLogsIn_formShouldBeEmpty() {
        webTestClient.get()
            .uri("http://localhost:" + port + "/")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("<input type=\"text\" id=\"firstName\" name=\"firstName\" value=\"\"");
                assert body.contains("<input type=\"text\" id=\"lastName\" name=\"lastName\" value=\"\"");
                assert body.contains("<input type=\"email\" id=\"email\" name=\"email\" value=\"\"");
            });
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenUserSubmitsData_dataShouldPersistAfterRefresh() {
        // Submit form data
        webTestClient.post()
            .uri("http://localhost:" + port + "/customer")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("firstName=John&lastName=Doe&email=john.doe@example.com")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", "/");

        // Check if data persisted
        webTestClient.get()
            .uri("http://localhost:" + port + "/")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("<input type=\"text\" id=\"firstName\" name=\"firstName\" value=\"John\"");
                assert body.contains("<input type=\"text\" id=\"lastName\" name=\"lastName\" value=\"Doe\"");
                assert body.contains("<input type=\"email\" id=\"email\" name=\"email\" value=\"john.doe@example.com\"");
            });
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenUserUpdatesData_updatedDataShouldPersistAfterRefresh() {
        // Submit initial data
        webTestClient.post()
            .uri("http://localhost:" + port + "/customer")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("firstName=John&lastName=Doe&email=john.doe@example.com")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", "/");

        // Update data
        webTestClient.post()
            .uri("http://localhost:" + port + "/customer")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("firstName=Jane&lastName=Smith&email=jane.smith@example.com")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", "/");

        // Check if updated data persisted
        webTestClient.get()
            .uri("http://localhost:" + port + "/")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("<input type=\"text\" id=\"firstName\" name=\"firstName\" value=\"Jane\"");
                assert body.contains("<input type=\"text\" id=\"lastName\" name=\"lastName\" value=\"Smith\"");
                assert body.contains("<input type=\"email\" id=\"email\" name=\"email\" value=\"jane.smith@example.com\"");
            });
    }
}