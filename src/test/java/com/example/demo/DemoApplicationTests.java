package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DemoApplication.class)
class DemoApplicationTests {

    @LocalServerPort
    private int port;

    private WebClient webClient;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    if (clientResponse.statusCode().is3xxRedirection()) {
                        return webClient.get().uri(clientResponse.headers().asHttpHeaders().getLocation()).exchange();
                    }
                    return Mono.just(clientResponse);
                }))
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void flywayMigrationsShouldBeApplied() throws Exception {
        // Check if the 'customers' table exists
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT COUNT(*) FROM customers";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                int count = resultSet.getInt(1);
                assertThat(count).isNotNull();
            }
        }
    }

    @Test
    @Disabled
    void securedPingShouldBeUnauthorized() {
        ClientResponse response = webClient.get().uri("/secured-ping").exchange().block();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Disabled
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void securedPingShouldBeAuthorized() {
        ClientResponse response = webClient.get().uri("/secured-ping").headers(headers -> headers.setBasicAuth("admin", "admin")).exchange().block();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
        String body = response.bodyToMono(String.class).block();
        assertThat(body).isEqualTo("Ping! You are authenticated.");
    }

    @Test
    void testFlywayMigration() {
        // Test logic to verify Flyway migration
    }
}
