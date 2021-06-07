package com.jnk2016.soulyyoubackend.savingsgoal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SavingsGoalControllerTest {
    @InjectMocks
    SavingsGoalController savingsGoalController;

    @Mock
    SavingsGoalService savingsGoalService;

    Authentication auth;

    HashMap<String, Object> body;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        auth = mock(Authentication.class);
        body = new HashMap<>();
    }

    @Test
    void shouldGetAllUserSavingsGoals() {
        when(savingsGoalService.getAllUserSavingsGoals(auth))
                .thenReturn(new ArrayList<>());

        assertEquals(200, savingsGoalController.getAllUserSavingsGoals(auth).getStatusCodeValue());
    }

    @Test
    void shouldCatchEntityNotFoundExceptionWhenGetAllUserSavingsGoals() {
        when(savingsGoalService.getAllUserSavingsGoals(auth))
                .thenThrow(new EntityNotFoundException());

        assertEquals(401, savingsGoalController.getAllUserSavingsGoals(auth).getStatusCodeValue());
    }

    @Test
    void shouldCreateNewSavingsGoal() {
        when(savingsGoalService.toJsonBody(savingsGoalService.createNewSavingsGoal(auth, body)))
                .thenReturn(body);

        assertEquals(201, savingsGoalController.createNewSavingsGoal(auth, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchEntityNotFoundExceptionWhenCreateNewSavingsGoal() {
        when(savingsGoalService.toJsonBody(savingsGoalService.createNewSavingsGoal(auth, body)))
                .thenThrow(new EntityNotFoundException());

        assertEquals(401, savingsGoalController.createNewSavingsGoal(auth, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchNullPointerExceptionWhenCreateNewSavingsGoal() {
        when(savingsGoalService.toJsonBody(savingsGoalService.createNewSavingsGoal(auth, body)))
                .thenThrow(new NullPointerException());

        assertEquals(400, savingsGoalController.createNewSavingsGoal(auth, body).getStatusCodeValue());
    }

    @Test
    void shouldUpdateSavingsGoal() {
        when(savingsGoalService.toJsonBody(savingsGoalService.updateSavingsGoal(1L, body)))
                .thenReturn(body);

        assertEquals(200, savingsGoalController.updateSavingsGoal(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchNullPointerExceptionWhenUpdateSavingsGoal() {
        when(savingsGoalService.toJsonBody(savingsGoalService.updateSavingsGoal(1L, body)))
                .thenThrow(new NullPointerException());

        assertEquals(410, savingsGoalController.updateSavingsGoal(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchIllegalArgExceptionWhenUpdateSavingsGoal() {
        when(savingsGoalService.toJsonBody(savingsGoalService.updateSavingsGoal(1L, body)))
                .thenThrow(new IllegalArgumentException());

        assertEquals(400, savingsGoalController.updateSavingsGoal(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldDeleteSavingsGoal() {
        assertEquals(200, savingsGoalController.deleteSavingsGoal(1L).getStatusCodeValue());
    }

    @Test
    void shouldCatchNullPointerExceptionWhenDeleteSavingsGoal() {
        when(savingsGoalService.deleteSavingsGoal(1L))
                .thenThrow(new NullPointerException());

        assertEquals(410, savingsGoalController.deleteSavingsGoal(1L).getStatusCodeValue());
    }
}