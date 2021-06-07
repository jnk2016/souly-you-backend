package com.jnk2016.soulyyoubackend.mood;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoodControllerTest {
    @InjectMocks
    MoodController moodController;

    @Mock
    MoodService moodService;

    Authentication auth;
    HashMap<String, Object> body;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auth = mock(Authentication.class);
        body = new HashMap<>();
    }

    @Test
    void shouldCreateNewMood() {
        when(moodService.toJsonBody(moodService.createNewMood(auth, body)))
                .thenReturn(body);

        assertEquals(201, moodController.createNewMood(auth, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchEntityNotFoundExcWhenCreateNewMood() {
        when(moodService.toJsonBody(moodService.createNewMood(auth, body)))
                .thenThrow(new EntityNotFoundException());

        assertEquals(401, moodController.createNewMood(auth, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchIllegalArgExcWhenCreateNewMood() {
        when(moodService.toJsonBody(moodService.createNewMood(auth, body)))
                .thenThrow(new IllegalArgumentException());

        assertEquals(400, moodController.createNewMood(auth, body).getStatusCodeValue());
    }

    @Test
    void shouldGetLatestMoodEntry() {
        when(moodService.toJsonBody(moodService.getLatestMoodEntry(auth)))
                .thenReturn(body);

        assertEquals(200, moodController.getLatestMoodEntry(auth).getStatusCodeValue());
    }

    @Test
    void shouldCatchEntityNotFoundExcWhenGetLatestMoodEntry() {
        when(moodService.toJsonBody(moodService.getLatestMoodEntry(auth)))
                .thenThrow(new EntityNotFoundException());

        assertEquals(401, moodController.getLatestMoodEntry(auth).getStatusCodeValue());
    }

    @Test
    void shouldReturnNoContentWhenGetLatestMoodEntry() {
        when(moodService.toJsonBody(moodService.getLatestMoodEntry(auth)))
                .thenThrow(new NullPointerException());

        assertEquals(204, moodController.getLatestMoodEntry(auth).getStatusCodeValue());
    }

    @Test
    void shouldUpdateMood() {
        when(moodService.toJsonBody(moodService.updateMood(1L, body)))
                .thenReturn(body);

        assertEquals(200, moodController.updateMood(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchEntityNotFoundExcWhenUpdateMood() {
        when(moodService.toJsonBody(moodService.updateMood(1L, body)))
                .thenThrow(new NullPointerException());

        assertEquals(410, moodController.updateMood(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchIllegalArgExcWhenUpdateMood() {
        when(moodService.toJsonBody(moodService.updateMood(1L, body)))
                .thenThrow(new IllegalArgumentException());

        assertEquals(400, moodController.updateMood(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldDeleteMood() {
        when(moodService.toJsonBody(moodService.deleteMood(1L)))
                .thenReturn(body);

        assertEquals(200, moodController.updateMood(1L, body).getStatusCodeValue());
    }

    @Test
    void shouldCatchEntityNotFoundExcWhenDeleteMood() {
        when(moodService.toJsonBody(moodService.deleteMood(1L)))
                .thenThrow(new NullPointerException());

        assertEquals(410, moodController.updateMood(1L, body).getStatusCodeValue());
    }
}