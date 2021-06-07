package com.jnk2016.soulyyoubackend.monthlybudget;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonthlyBudgetControllerTest {
    @InjectMocks
    MonthlyBudgetController monthlyBudgetController;

    @Mock
    MonthlyBudgetService monthlyBudgetService;

    MonthlyBudget monthlyBudget;
    HashMap<String, Object> jsonResponse;
    Authentication auth;
    ResponseEntity<HashMap<String,Object>> response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ApplicationUser user = new ApplicationUser();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setUserId(1);
        user.setUsername("bioround");
        user.setFirstname("Nikhil");
        user.setLastname("Kim");
        user.setPassword(bCryptPasswordEncoder.encode("Password"));
        user.setDateJoined(LocalDate.now());
        monthlyBudget = new MonthlyBudget();monthlyBudget = new MonthlyBudget();
        monthlyBudget.setBudgetGoal(3000);
        monthlyBudget.setUser(user);
        monthlyBudget.setMonth(4);
        monthlyBudget.setYear(2021);
        monthlyBudget.setIncome(2000);
        monthlyBudget.setBalance(1020);
        monthlyBudget.setBudgetId(1);
        monthlyBudget.setExpenses(980);
        monthlyBudget.setBudgetRemaining(2020);

        jsonResponse = new HashMap<>();
        jsonResponse.put("budget_id", 1L);
        jsonResponse.put("balance", 1020.00);
        jsonResponse.put("income", 2000.00);
        jsonResponse.put("expenses", 980.00);
        jsonResponse.put("budget_goal", 3000.00);
        jsonResponse.put("budget_remaining", 1L);
        jsonResponse.put("month", 4);
        jsonResponse.put("year", 2021);
        jsonResponse.put("user_id", 1L);
        jsonResponse.put("transactions", new ArrayList<>());

        auth = Mockito.mock(Authentication.class);
    }

    @Test
    void shouldGetCurrentBudget() {
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget);
        when(monthlyBudgetService.budgetToJsonBody(monthlyBudget)).thenReturn(jsonResponse);

        response = monthlyBudgetController.getCurrentBudget(auth);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldNOTGetCurrentBudget() {
        when(monthlyBudgetService.getCurrentBudget(auth)).thenThrow(new NullPointerException("Budget not found"));

        response = monthlyBudgetController.getCurrentBudget(auth);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Budget not found", response.getBody().get("message"));
    }

    @Test
    void shouldCreateNewBudget() {
        when(monthlyBudgetService.createNewBudget(auth)).thenReturn(monthlyBudget);
        when(monthlyBudgetService.budgetToJsonBody(monthlyBudget)).thenReturn(jsonResponse);

        response = monthlyBudgetController.createNewBudget(auth);
        assertEquals(201, response.getStatusCodeValue());
    }

    @Test
    void shouldNOTCreateNewBudgetWhenAlreadyCurrent() {
        when(monthlyBudgetService.createNewBudget(auth)).thenReturn(null);

        response = monthlyBudgetController.createNewBudget(auth);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("This month's budget already exists!", response.getBody().get("message"));
    }

    @Test
    void shouldNOTCreateNewBudgetWhenUserNotFound() {
        when(monthlyBudgetService.createNewBudget(auth)).thenThrow(new EntityNotFoundException("User not found"));

        response = monthlyBudgetController.createNewBudget(auth);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    void shouldGetBudgetByDate() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("month", 4);
        body.put("year", 2021);

        when(monthlyBudgetService.getBudgetByDate(auth, 4, 2021)).thenReturn(monthlyBudget);
        when(monthlyBudgetService.budgetToJsonBody(monthlyBudget)).thenReturn(jsonResponse);

        response = monthlyBudgetController.getBudgetByDate(auth, body);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldNOTGetBudgetByDateWhenIncorrectParameters() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("month", 4);
        body.put("year", 2021);

        when(monthlyBudgetService.getBudgetByDate(auth, 4, 2021)).thenThrow(new IllegalArgumentException("Incorrect parameters"));

        response = monthlyBudgetController.getBudgetByDate(auth, body);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Incorrect parameters", response.getBody().get("message"));
    }

    @Test
    void shouldUpdateBudgetGoal() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget_goal", 3500.00);

        when(monthlyBudgetService.changeBudgetGoal(1L, body)).thenReturn(monthlyBudget);

        response = monthlyBudgetController.updateBudgetGoal(1L, body);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("balance"));
        assertTrue(response.getBody().containsKey("budget_remaining"));
    }

    @Test
    void shouldNOTUpdateBudgetGoalWhenEntityException() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget_goal", 3500.00);

        when(monthlyBudgetService.changeBudgetGoal(1L, body)).thenThrow(new IllegalArgumentException());

        response = monthlyBudgetController.updateBudgetGoal(1L, body);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("message"));
    }

    @Test
    void shouldNOTUpdateBudgetGoalWhenNullException() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget_goal", 3500.00);

        when(monthlyBudgetService.changeBudgetGoal(1L, body)).thenThrow(new NullPointerException());

        response = monthlyBudgetController.updateBudgetGoal(1L, body);
        assertEquals(410, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("message"));
    }
}