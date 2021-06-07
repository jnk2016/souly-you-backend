package com.jnk2016.soulyyoubackend.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    ApplicationUser applicationUser;
    Authentication auth;
    HashMap<String, String> body;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        applicationUser = new ApplicationUser();
        applicationUser.setUserId(1L);
        applicationUser.setUsername("bioround");
        applicationUser.setPassword(bCryptPasswordEncoder.encode("paypal123"));
        applicationUser.setFirstname("Nikhil");
        applicationUser.setLastname("Suri");
        applicationUser.setDateJoined(LocalDate.now());

        auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("bioround");

        body = new HashMap<>();
        body.put("username", "bioround");
        body.put("password", "paypal123");
        body.put("firstname", "Nikhil");
        body.put("lastname", "Suri");
    }

    @Test
    void shouldReturnApplicationUser() {
        when(applicationUserRepository.findByUsername("bioround")).thenReturn(Optional.of(applicationUser));

        assertEquals(applicationUser, userService.getApplicationUser(auth));
    }

    @Test
    void shouldNOTReturnApplicationUser() {
        when(applicationUserRepository.findByUsername("bioround")).thenThrow(new EntityNotFoundException("No such user exists"));

        assertThrows(EntityNotFoundException.class,
                ()-> userService.getApplicationUser(auth));
    }

    @Test
    void userShouldAlreadyExist() {
        when(applicationUserRepository.findByUsername("bioround")).thenReturn(Optional.of(applicationUser));

        assertFalse(userService.newUser(body));
    }

    @Test
    void shouldMakeNewUser() {
        body.put("username", "bioround2");
        when(applicationUserRepository.findByUsername("bioround")).thenReturn(Optional.of(applicationUser));

        assertTrue(userService.newUser(body));
    }

    @Test
    void shouldGetDateJoined() {
        when(applicationUserRepository.findByUsername("bioround")).thenReturn(Optional.of(applicationUser));

        assertEquals(LocalDate.now(), userService.getDateJoined(auth));
    }

    @Test
    void shouldNOTGetDateJoined() {
        when(applicationUserRepository.findByUsername("bioround")).thenThrow(new EntityNotFoundException("No such user found"));

        assertThrows(EntityNotFoundException.class,
                ()-> userService.getDateJoined(auth));
    }

    @Test
    void shouldConvertToJsonBody() {
        List<String> filters = Stream.of("password", "dateJoined", "budgets", "transactions", "savingsGoals")
                .collect(Collectors.toList());

        HashMap<String, Object> actual = userService.toJsonBody(ApplicationUser.class, applicationUser, filters);

        actual.remove("password"); actual.remove("dateJoined"); actual.remove("budgets"); actual.remove("transactions");

        HashMap<String, Object> expected = new HashMap<>();
        expected.put("user_id", 1L); expected.put("username", "bioround"); expected.put("firstname", "Nikhil"); expected.put("lastname", "Suri");

        assertEquals(actual, expected);
    }

    @Test
    void toUnderscoreName() {
        assertEquals("budget_remaining", userService.toUnderscoreName("budgetRemaining"));
    }

}