package com.personalfinance.personalfinancetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.personalfinancetracker.dto.LoginRequest;
import com.personalfinance.personalfinancetracker.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that verify the full request/response cycle through
 * the real Spring Security filter chain, controllers, services, and
 * repository layer against a real database connection. Unlike unit tests
 * which mock dependencies, these tests confirm that all layers work
 * together correctly end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;
    private static Long testCategoryId;

    /**
     * Registers and logs in a test user before the suite runs, storing
     * the JWT for use in authenticated test cases. Runs first due to
     * @Order(1) so subsequent tests have a valid token.
     */
    @Test
    @Order(1)
    void setup_registerAndLogin() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setFirstName("Test");
        register.setLastName("User");
        register.setUsername("integrationuser");
        register.setEmail("integration@example.com");
        register.setPassword("Test1234!");

        try {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(register)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            // User may already exist from a previous test run — that's fine
        }

        LoginRequest login = new LoginRequest();
        login.setUsername("integrationuser");
        login.setPassword("Test1234!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(body).get("token").asText();

        // Grab a real category ID for use in later tests
        MvcResult catResult = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String catBody = catResult.getResponse().getContentAsString();
        testCategoryId = objectMapper.readTree(catBody).get(0).get("id").asLong();
    }

    /**
     * Verifies that hitting a protected endpoint without a JWT returns
     * 401 Unauthorized, confirming the Security filter chain is active.
     */
    @Test
    @Order(2)
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that hitting the dashboard endpoint without a JWT also
     * returns 401, confirming protection extends beyond just transactions.
     */
    @Test
    @Order(3)
    void dashboardEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that an authenticated user can retrieve their transaction
     * list and receives a 200 OK response.
     */
    @Test
    @Order(4)
    void getTransactions_withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Verifies the full budget creation flow end to end: authenticated
     * request goes through security, controller, service, and repository,
     * and returns 201 Created with the expected response fields.
     */
    @Test
    @Order(5)
    void createBudget_withValidToken_returns201() throws Exception {
        String budgetJson = String.format(
                "{\"categoryId\":%d,\"amountLimit\":500,\"month\":7,\"year\":2026}",
                testCategoryId);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(budgetJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountLimit").value(500))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    /**
     * Verifies that filtering transactions by keyword returns 200 OK,
     * confirming the filter parameters are correctly passed through the
     * controller into the service layer.
     */
    @Test
    @Order(6)
    void getTransactions_withKeywordFilter_returns200() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}