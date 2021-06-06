package com.jnk2016.soulyyoubackend.savingsgoal;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import com.jnk2016.soulyyoubackend.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SavingsGoalServiceTest {
    @InjectMocks
    SavingsGoalService savingsGoalService;

    @Mock
    UserService userService;
    @Mock
    SavingsGoalRepository savingsGoalRepository;

    Authentication auth;

    ApplicationUser user;

    SavingsGoal savingsGoal1;
    SavingsGoal savingsGoal2;

    List<HashMap<String, Object>> responseList;

    void setupUser() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user = new ApplicationUser(
                1L,
                "jaxnk2020",
                bCryptPasswordEncoder.encode("password"),
                "Jackson",
                "Kim",
                LocalDate.now(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    void setupSavingsGoal() {
        savingsGoal1 = new SavingsGoal(
               1L,
               "New Laptop",
                1000.00,
                750.00,
                false,
                user
        );
        savingsGoal2 = new SavingsGoal(
               2L,
               "Puppy fund",
                3000.00,
                1500.00,
                false,
                user
        );
        List<SavingsGoal> savingsGoals = Stream
                .of(savingsGoal1, savingsGoal2)
                .collect(Collectors.toList());
        user.setSavingsGoals(savingsGoals);
    }

    void setupResponseList() {
        HashMap<String, Object> response1 = new HashMap<>();
        response1.put("savings_id", 1L);
        response1.put("name", "New Laptop");
        response1.put("goal_amount", 1000.00);
        response1.put("saved_amount", 750.00);
        response1.put("complete", false);
        response1.put("user_id", 1L);

        HashMap<String, Object> response2 = new HashMap<>();
        response2.put("savings_id", 2L);
        response2.put("name", "Puppy Fund");
        response2.put("goal_amount", 3000.00);
        response2.put("saved_amount", 1500.00);
        response2.put("complete", false);
        response2.put("user_id", 1L);

        responseList = Stream
                .of(response1, response2)
                .collect(Collectors.toList());
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        auth = Mockito.mock(Authentication.class);

        setupUser();
        setupSavingsGoal();
        setupResponseList();

        when(userService.toJsonBody(SavingsGoal.class, savingsGoal1, Stream.of("user").collect(Collectors.toList())))
                .thenReturn(responseList.get(0));
        when(userService.toJsonBody(SavingsGoal.class, savingsGoal2, Stream.of("user").collect(Collectors.toList())))
                .thenReturn(responseList.get(1));
    }

    @Test
    void shouldGetAllUserSavingsGoals() {
        when(userService.getApplicationUser(auth))
                .thenReturn(user);
        when(savingsGoalRepository.findByUser(user))
                .thenReturn(user.getSavingsGoals());

        assertEquals(responseList, savingsGoalService.getAllUserSavingsGoals(auth));
    }

    @Test
    void shouldNOTGetAllUserSavingsGoalsWhenNullUser() {
        when(userService.getApplicationUser(auth))
                .thenReturn(null);

        assertThrows(NullPointerException.class,
                ()-> savingsGoalService.getAllUserSavingsGoals(auth));
    }

    @Test
    void shouldNOTGetAllUserSavingsGoalsWhenNullSavingsGoals() {
        when(savingsGoalRepository.findByUser(user))
                .thenReturn(null);

        assertThrows(NullPointerException.class,
                ()-> savingsGoalService.getAllUserSavingsGoals(auth));
    }

    @Test
    void findSavingsGoalById() {
    }

    @Test
    void createNewSavingsGoal() {
    }

    @Test
    void updateSavingsGoal() {
    }

    @Test
    void deleteSavingsGoal() {
    }
}