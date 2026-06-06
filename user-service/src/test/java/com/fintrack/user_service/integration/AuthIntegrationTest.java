package com.fintrack.user_service.integration;

import com.fintrack.user_service.dto.LoginRequest;
import com.fintrack.user_service.dto.RegisterRequest;
import com.fintrack.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("fintrack_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register and login successfully end to end")
    void registerAndLogin_Success() {
        // Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Pranav");
        registerRequest.setEmail("pranav@test.com");
        registerRequest.setPassword("password123");

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            registerRequest,
            String.class
        );

        assertThat(registerResponse.getStatusCode())
            .isEqualTo(HttpStatus.OK);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("pranav@test.com");
        loginRequest.setPassword("password123");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/login",
            loginRequest,
            String.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).contains("token");
    }

    @Test
    @DisplayName("Should return 400 when registering duplicate email")
    void register_DuplicateEmail_Returns400() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Pranav");
        request.setEmail("pranav@test.com");
        request.setPassword("password123");

        // First registration
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            request, String.class);

        // Second registration — same email
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            request, String.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}