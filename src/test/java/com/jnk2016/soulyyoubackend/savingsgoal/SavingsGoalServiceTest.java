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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
                .thenThrow(new EntityNotFoundException("No such user found or token expired"));

        assertThrows(EntityNotFoundException.class,
                ()-> savingsGoalService.getAllUserSavingsGoals(auth));
    }

    @Test
    void shouldReturnEmptyListWhenUserSavingsGoals() {
        when(savingsGoalRepository.findByUser(user))
                .thenReturn(null);

        assertEquals(new ArrayList<>(), savingsGoalService.getAllUserSavingsGoals(auth));
    }

    @Test
    void shouldCreateNewSavingsGoal() {
        HashMap<String, Object> body = (HashMap<String, Object>) responseList.get(0).clone();
        body.remove("savings_id");
        body.remove("complete");
        body.remove("user_id");

        when(userService.getApplicationUser(auth))
                .thenReturn(user);

        assertEquals(savingsGoal1.getGoalAmount(), savingsGoalService.createNewSavingsGoal(auth, body).getGoalAmount());
        assertEquals(savingsGoal1.getName(), savingsGoalService.createNewSavingsGoal(auth, body).getName());
        assertEquals(savingsGoal1.getSavedAmount(), savingsGoalService.createNewSavingsGoal(auth, body).getSavedAmount());
        assertEquals(savingsGoal1.isComplete(), savingsGoalService.createNewSavingsGoal(auth, body).isComplete());
    }

    @Test
    void shouldNOTCreateNewSavingsGoalWhenIncorrectUser() {
        HashMap<String, Object> body = (HashMap<String, Object>) responseList.get(0).clone();
        body.remove("savings_id");
        body.remove("complete");
        body.remove("user_id");

        when(userService.getApplicationUser(auth))
                .thenThrow(new NullPointerException("No such user found or token expired"));

        assertThrows(NullPointerException.class,
                ()-> savingsGoalService.createNewSavingsGoal(auth, body));
    }

    @Test
    void shouldNOTCreateNewSavingsGoalWhenIncorrectBody() {
        HashMap<String, Object> body = (HashMap<String, Object>) responseList.get(0).clone();
        body.remove("savings_id");
        body.remove("user_id");

        when(userService.getApplicationUser(auth))
                .thenReturn(user);

        body.put("complete", true);
        Exception thrown = assertThrows(IllegalArgumentException.class,
                ()-> savingsGoalService.createNewSavingsGoal(auth, body));
        assertEquals("Incorrect formatting of request body", thrown.getMessage());
        body.remove("complete");

        body.remove("saved_amount");
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> savingsGoalService.createNewSavingsGoal(auth, body));
        assertEquals("Incorrect formatting of request body", thrown.getMessage());

        body.put("saved_amount", -1.00);
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> savingsGoalService.createNewSavingsGoal(auth, body));
        assertEquals("Invalid value for saved and/or goal amount", thrown.getMessage());
    }

    @Test
    void shouldUpdateSavingsGoal() {
        HashMap<String, Object> body = (HashMap<String, Object>) responseList.get(0).clone();
        body.remove("savings_id");
        body.remove("complete");
        body.remove("user_id");
        body.put("goal_amount", 1500.00);
        body.put("saved_amount", 810.00);
        body.put("name", "new laptop + graphics card");

        SavingsGoal actual = savingsGoal1;
        actual.setGoalAmount(1500.00);
        actual.setSavedAmount(810.00);
        actual.setName("new laptop + graphics card");

        when(savingsGoalRepository.findById(1L))
                .thenReturn(Optional.of(savingsGoal1));

        assertEquals(actual.getGoalAmount(), savingsGoalService.updateSavingsGoal(1L, body).getGoalAmount());
        assertEquals(actual.getName(), savingsGoalService.updateSavingsGoal(1L, body).getName());
        assertEquals(actual.getSavedAmount(), savingsGoalService.updateSavingsGoal(1L, body).getSavedAmount());
        assertEquals(false, savingsGoalService.updateSavingsGoal(1L, body).isComplete());

        body.put("saved_amount", 1500.00);
        assertEquals(true, savingsGoalService.updateSavingsGoal(1L, body).isComplete());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenUpdateSavingsGoal() {
        HashMap<String, Object> body = (HashMap<String, Object>) responseList.get(0).clone();
        body.remove("savings_id");
        body.remove("complete");
        body.remove("user_id");
        body.put("goal_amount", 1500.00);
        body.put("saved_amount", 810.00);
        body.put("name", "new laptop + graphics card");

        when(savingsGoalRepository.findById(1L))
                .thenThrow(new NullPointerException());

        assertThrows(NullPointerException.class,
                ()-> savingsGoalService.updateSavingsGoal(1L, body));
    }

    @Test
    void shouldThrowIllegalArgExceptionWhenUpdateSavingsGoal() {
        HashMap<String, Object> body = (HashMap<String, Object>) responseList.get(0).clone();
        body.remove("savings_id");
        body.remove("complete");
        body.remove("user_id");
        body.remove("name");

        when(savingsGoalRepository.findById(1L))
                .thenReturn(Optional.of(savingsGoal1));

        body.put("goal_amount", 0.00);
        Exception thrown = assertThrows(IllegalArgumentException.class,
                ()-> savingsGoalService.updateSavingsGoal(1L, body));
        assertEquals("Invalid value for saved and/or goal amount", thrown.getMessage());
        body.put("goal_amount", 10.00);

        body.put("saved_amount", -1.00);
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> savingsGoalService.updateSavingsGoal(1L, body));
        assertEquals("Invalid value for saved and/or goal amount", thrown.getMessage());

        body.remove("goal_amount");
        body.remove("saved_amount");
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> savingsGoalService.updateSavingsGoal(1L, body));
        assertEquals("Please provide at least one attribute to update", thrown.getMessage());
    }
}