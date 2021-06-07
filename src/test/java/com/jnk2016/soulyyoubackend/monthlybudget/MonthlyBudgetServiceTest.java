package com.jnk2016.soulyyoubackend.monthlybudget;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonthlyBudgetServiceTest {
    @InjectMocks
    MonthlyBudgetService monthlyBudgetService;

    @Mock
    MonthlyBudgetRepository monthlyBudgetRepository;

    @Mock
    UserService userService;

    MonthlyBudget monthlyBudget;

    BCryptPasswordEncoder bCryptPasswordEncoder;

    ApplicationUser user;

    List<MonthlyBudget> monthlyBudgets;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user = new ApplicationUser();

        user.setUserId(1);
        user.setUsername("bioround");
        user.setFirstname("Nikhil");
        user.setLastname("Kim");
        user.setPassword(bCryptPasswordEncoder.encode("Password"));
        user.setDateJoined(LocalDate.now());

        monthlyBudget = new MonthlyBudget();
        monthlyBudget.setBudgetGoal(3000);
        monthlyBudget.setUser(user);
        monthlyBudget.setMonth(3);
        monthlyBudget.setYear(2021);
        monthlyBudget.setIncome(0);
        monthlyBudget.setBalance(0);
        monthlyBudget.setBudgetId(1);
        monthlyBudget.setExpenses(0);
        monthlyBudget.setBudgetRemaining(3000);

        monthlyBudgets = new ArrayList<>();
        monthlyBudgets.add(monthlyBudget);
        user.setBudgets(monthlyBudgets);
    }

    @Test
    void shouldInitializeFirstMonthlyBudget() {
        assertEquals(LocalDate.now().getMonthValue(), monthlyBudgetService.initializeFirstMonthlyBudget(user).getMonth());
        assertEquals(2021, monthlyBudgetService.initializeFirstMonthlyBudget(user).getYear());
        assertEquals(0, monthlyBudgetService.initializeFirstMonthlyBudget(user).getBalance());
    }

    @Test
    void shouldAddNextBudget() {
        when(monthlyBudgetRepository.findFirstByUserOrderByBudgetIdDesc(user)).thenReturn(monthlyBudget);
        assertEquals(LocalDate.now().getMonthValue(), monthlyBudgetService.addNextBudget(user).getMonth());
        assertEquals(3000, monthlyBudgetService.addNextBudget(user).getBudgetGoal());
    }

    @Test
    void createNewBudgetShouldReturnFirstBudget() {
        ApplicationUser newUser = new ApplicationUser();
        newUser.setUserId(2);
        newUser.setUsername("jaxnk2020");
        newUser.setFirstname("Jackson");
        newUser.setLastname("Suri");
        newUser.setPassword(bCryptPasswordEncoder.encode("Password"));
        newUser.setDateJoined(LocalDate.now());

        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(newUser);
        assertEquals(0, monthlyBudgetService.createNewBudget(auth).getBudgetGoal());
    }

    @Test
    void createNewBudgetShouldReturnNextBudget() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user);
        when(monthlyBudgetRepository.findByUserAndMonthAndYear(user, 4, 2021)).thenReturn(null);
        when(monthlyBudgetRepository.findFirstByUserOrderByBudgetIdDesc(user)).thenReturn(monthlyBudget);

        assertEquals(3000, monthlyBudgetService.createNewBudget(auth).getBudgetGoal());
        assertEquals(LocalDate.now().getMonthValue(), monthlyBudgetService.createNewBudget(auth).getMonth());
    }

    @Test void createNewBudgetShouldThrowException() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> monthlyBudgetService.createNewBudget(auth));
    }

    @Test
    void shouldGetCurrentBudget() {
        monthlyBudget.setMonth(LocalDate.now().getMonthValue());
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user);
        when(monthlyBudgetRepository.findByUserAndMonthAndYear(user, LocalDate.now().getMonthValue(), LocalDate.now().getYear())).thenReturn(monthlyBudget);

        assertEquals(3000, monthlyBudgetService.getCurrentBudget(auth).getBudgetGoal());
        assertEquals(LocalDate.now().getMonthValue(), monthlyBudgetService.getCurrentBudget(auth).getMonth());
    }

    @Test
    void shouldNOTGetCurrentBudgetWhenNoCurrent() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user);
        when(monthlyBudgetRepository.findByUserAndMonthAndYear(user, 5, 2021)).thenReturn(null);

        NullPointerException thrown = assertThrows(NullPointerException.class, ()-> monthlyBudgetService.getCurrentBudget(auth));

        assertEquals("Budget not found", thrown.getMessage());
    }

    @Test
    void shouldNOTGetCurrentBudgetWhenUserNotFound() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class, ()-> monthlyBudgetService.getCurrentBudget(auth));

        assertEquals("User not found", thrown.getMessage());
    }

    @Test
    void getCurrentBudgetShouldReturnCurrentBudget() {
        MonthlyBudget currentBudget = new MonthlyBudget();
        currentBudget.setBudgetGoal(3500);
        currentBudget.setUser(user);
        currentBudget.setMonth(LocalDate.now().getMonthValue());
        currentBudget.setYear(2021);
        currentBudget.setIncome(0);
        currentBudget.setBalance(0);
        currentBudget.setBudgetId(2);
        currentBudget.setExpenses(0);
        currentBudget.setBudgetRemaining(3500);

        monthlyBudgets.add(currentBudget);
        user.setBudgets(monthlyBudgets);

        Authentication auth = Mockito.mock(Authentication.class);

        when(userService.getApplicationUser(auth)).thenReturn(user);
        when(monthlyBudgetRepository.findByUserAndMonthAndYear(user, LocalDate.now().getMonthValue(), 2021)).thenReturn(currentBudget);

        assertEquals(3500, monthlyBudgetService.getCurrentBudget(auth).getBudgetGoal());
        assertEquals(LocalDate.now().getMonthValue(), monthlyBudgetService.getCurrentBudget(auth).getMonth());
    }

    @Test
    void shouldGetBudgetById() {
        when(monthlyBudgetRepository.findById(1L)).thenReturn(Optional.of(monthlyBudget));

        assertEquals(monthlyBudget.getBudgetGoal(), monthlyBudgetService.getBudgetById(1L).getBudgetGoal());
    }

    @Test
    void shouldChangeBudgetGoalAndUpdateRemaining() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget_goal", 3500.00);
        when(monthlyBudgetRepository.findById(1L)).thenReturn(Optional.of(monthlyBudget));

        monthlyBudget.setExpenses(1480);
        assertEquals(3500, monthlyBudgetService.changeBudgetGoal(1L, body).getBudgetGoal());
        assertEquals(2020, monthlyBudget.getBudgetRemaining());
    }

    @Test
    void changeBudgetGoalShouldNOTUpdateWhenInvalidBody() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget", 3500.00);

        Exception thrown = assertThrows(
                IllegalArgumentException.class, ()-> monthlyBudgetService.changeBudgetGoal(1L, body));
        assertEquals("Incorrect JSON body", thrown.getMessage());

        body.put("budget_goal", 3400.00);
        thrown = assertThrows(
                IllegalArgumentException.class, ()-> monthlyBudgetService.changeBudgetGoal(1L, body));
        assertEquals("Incorrect JSON body", thrown.getMessage());

        body.remove("budget");
        body.put("budget_goal", "Some string");
        assertEquals("Incorrect JSON body", thrown.getMessage());
    }

    @Test
    void changeBudgetGoalShouldNOTUpdateWhenInvalidBodyValue() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget_goal", null);
        Exception thrown = assertThrows(
                IllegalArgumentException.class, ()-> monthlyBudgetService.changeBudgetGoal(1L, body));
        assertEquals("Incorrect JSON body", thrown.getMessage());

        body.put("budget_goal", -1.00);
        thrown = assertThrows(
                IllegalArgumentException.class, ()-> monthlyBudgetService.changeBudgetGoal(1L, body));
        assertEquals("Invalid value for budget_goal", thrown.getMessage());
    }

    @Test
    void changeBudgetGoalShouldNOTUpdateWhenInvalidBudgetId() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("budget_goal", 3500.00);

        when(monthlyBudgetRepository.findById(1L)).thenThrow(new NullPointerException());

        Exception thrown = assertThrows(
                NullPointerException.class, ()-> monthlyBudgetService.changeBudgetGoal(1L, body));
        assertEquals("Budget not found", thrown.getMessage());
        assertEquals(NullPointerException.class, thrown.getClass());
    }

    @Test
    void shouldUpdateBalance() {
        monthlyBudget.setIncome(2000);
        monthlyBudget.setExpenses(980);
        assertEquals(1020, monthlyBudgetService.updateBalance(monthlyBudget));

    }

    @Test
    void updateIncome() {
        monthlyBudget.setExpenses(980);
        assertEquals(2000, monthlyBudgetService.updateIncome(monthlyBudget, 2000));
        assertEquals(1020, monthlyBudget.getBalance());
    }

    @Test
    void updateExpenses() {
        monthlyBudget.setIncome(2000);
        assertEquals(980, monthlyBudgetService.updateExpenses(monthlyBudget, 980));
        assertEquals(1020, monthlyBudget.getBalance());
        assertEquals(2020, monthlyBudget.getBudgetRemaining());
    }

    @Test
    void shouldReturnNullTransactionsWhenBudgetToJsonBody() {
        monthlyBudget.setTransactions(new ArrayList<>());
        assertEquals(new ArrayList<>(), monthlyBudgetService.budgetToJsonBody(monthlyBudget).get("transactions"));
    }

    @Test
    void getBudgetByDateShouldReturnNullWhenInvalidUser() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenThrow(new EntityNotFoundException("User not found"));
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class, ()-> monthlyBudgetService.getBudgetByDate(auth, 1, 2019));
        assertEquals("User not found", thrown.getMessage());
    }

    @Test
    void getBudgetByDateShouldReturnNullWhenInvalidDate() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user);
        when(monthlyBudgetRepository.findByUserAndMonthAndYear(user, 1, 2019)).thenReturn(null);
        NullPointerException thrown = assertThrows(
                NullPointerException.class, ()-> monthlyBudgetService.getBudgetByDate(auth, 1, 2019));
        assertEquals("Budget not found", thrown.getMessage());
    }

    @Test
    void getBudgetByDateShouldReturnBudget() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user);
        when(monthlyBudgetRepository.findByUserAndMonthAndYear(user, 3, 2021)).thenReturn(monthlyBudget);
        assertEquals(monthlyBudget, monthlyBudgetService.getBudgetByDate(auth, 3, 2021));
    }

}