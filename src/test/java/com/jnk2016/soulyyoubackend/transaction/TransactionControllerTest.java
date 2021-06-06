package com.jnk2016.soulyyoubackend.transaction;

import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {
    @InjectMocks
    TransactionController transactionController;

    @Mock
    TransactionService transactionService;

    Authentication auth;

    ApplicationUser user1;
    ApplicationUser user2;

    MonthlyBudget monthlyBudget1;
    MonthlyBudget monthlyBudget2;
    MonthlyBudget monthlyBudget3;

    List<MonthlyBudget> budgets1;
    List<MonthlyBudget> budgets2;

    List<Transaction> transactions1;
    List<Transaction> transactions2;
    List<Transaction> transactions3;

    void initializeBudgetsAndUsers() {
        user1 = new ApplicationUser();
        user2 = new ApplicationUser();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        monthlyBudget1 = new MonthlyBudget();
        monthlyBudget2 = new MonthlyBudget();
        monthlyBudget3 = new MonthlyBudget();

        budgets1 = new ArrayList<>();
        budgets2 = new ArrayList<>();

        user1.setUserId(1);
        user1.setUsername("bioround");
        user1.setFirstname("Nikhil");
        user1.setLastname("Kim");
        user1.setPassword(bCryptPasswordEncoder.encode("Password"));
        user1.setDateJoined(LocalDate.now());

        monthlyBudget1.setUser(user1);
        monthlyBudget1.setMonth(3);
        monthlyBudget1.setYear(2021);
        monthlyBudget1.setBudgetGoal(3000);
        monthlyBudget1.setBudgetId(1);

        monthlyBudget2.setUser(user1);
        monthlyBudget2.setMonth(4);
        monthlyBudget2.setYear(2021);
        monthlyBudget2.setBudgetGoal(2500);
        monthlyBudget2.setBudgetId(2);

        budgets1.add(monthlyBudget1);
        budgets1.add(monthlyBudget2);
        user1.setBudgets(budgets1);

        user2.setUserId(2);
        user2.setUsername("jaxnk2020");
        user2.setFirstname("Jackson");
        user2.setLastname("Suri");
        user2.setPassword(bCryptPasswordEncoder.encode("Password"));
        user2.setDateJoined(LocalDate.now());

        monthlyBudget3.setUser(user2);
        monthlyBudget3.setMonth(4);
        monthlyBudget3.setYear(2021);
        monthlyBudget3.setBudgetGoal(3500);
        monthlyBudget3.setBudgetId(3);

        budgets2.add(monthlyBudget3);
        user2.setBudgets(budgets2);
    }

    void initializeTransactions() {
        transactions1 = new ArrayList<>();
        transactions2 = new ArrayList<>();
        transactions3 = new ArrayList<>();
        long i = 1;

        transactions1.add(new Transaction(i++, "1",500, "expense", true, LocalDateTime.now().minusDays(5), monthlyBudget1, user1));
        transactions1.add(new Transaction(i++,"2",100, "payment", false, LocalDateTime.now().minusDays(10), monthlyBudget1, user1));

        monthlyBudget1.setTransactions(transactions1);

        transactions2.add(new Transaction(i++,"3",600, "payment", false, LocalDateTime.now().plusDays(5), monthlyBudget2, user1));
        transactions2.add(new Transaction(i++,"4",100, "payment", false, LocalDateTime.now().plusDays(6), monthlyBudget2, user1));
        transactions2.add(new Transaction(i++,"5",70, "payment", true, LocalDateTime.now().plusDays(6), monthlyBudget2, user1));
        transactions2.add(new Transaction(i++,"6",25, "payment", true, LocalDateTime.now().plusDays(10), monthlyBudget2, user1));
        transactions2.add(new Transaction(i++,"7",50, "expense", true, LocalDateTime.now().plusDays(20), monthlyBudget2, user1));
        transactions2.add(new Transaction(i++,"8",25, "expense", true, LocalDateTime.now().plusDays(4), monthlyBudget2, user1));
        transactions2.add(new Transaction(i++,"9",500, "income", true, LocalDateTime.now().minusDays(21), monthlyBudget2, user1));

        monthlyBudget2.setTransactions(transactions2);
        monthlyBudget2.setIncome(500.00);
        monthlyBudget2.setExpenses(170.00);
        List<Transaction> user1Transactions = new ArrayList<>();
        user1Transactions.addAll(transactions1);
        user1Transactions.addAll(transactions2);
        user1.setTransactions(user1Transactions);

        transactions3.add(new Transaction(i++, "10",6400, "income", true, LocalDateTime.now().minusDays(3), monthlyBudget3, user2));
        transactions3.add(new Transaction(i, "11",3200, "income", false, LocalDateTime.now().minusDays(2), monthlyBudget3, user2));
        monthlyBudget3.setTransactions(transactions3);
        user2.setTransactions(transactions3);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializeBudgetsAndUsers();
        initializeTransactions();
        auth = Mockito.mock(Authentication.class);
    }

    public HashMap<String, Object> jsonResponse(Transaction transaction) {
        HashMap<String, Object> toJson = new HashMap<>();
        toJson.put("name", transaction.getName());
        toJson.put("amount", transaction.getAmount());
        toJson.put("type", transaction.getType());
        toJson.put("completed", transaction.isCompleted());
        toJson.put("timestamp", transaction.getTimestamp());
        toJson.put("user_id", transaction.getUser().getUserId());
        toJson.put("budget_id", transaction.getBudget().getBudgetId());

        return toJson;
    }
    public List<HashMap<String, Object>> jsonResponseList(List<Transaction> transactions) {
        List<HashMap<String, Object>> list = new ArrayList<>();
        for(Transaction transaction : transactions) {
            list.add(jsonResponse(transaction));
        }
        return list;
    }

    @Test
    void shouldCompletePayments() {
        String ids = "3,4";
        List<HashMap<String, Object>> actual = jsonResponseList(Stream.of(transactions2.get(0), transactions2.get(1))
                .collect(Collectors.toList()));
        HashMap<String, Object> response = new HashMap<>();
        response.put("new_expenses_total", 870.00);
        response.put("updated_payments", actual);
        response.put("total_payments_updated", 2);
        response.put("invalid_ids", new ArrayList<>());
        response.put("total_invalid_ids", 0);

        when(transactionService.completePayments(ids)).thenReturn(response);
        assertEquals(200, transactionController.completePayments(ids).getStatusCodeValue());
    }

    @Test
    void shouldNOTCompletePayments() {
        String ids = "92";
        when(transactionService.completePayments(ids)).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.completePayments(ids).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.completePayments(ids).getBody().get("message"));
    }

    @Test
    void shouldReceiveIncomes() {
        String ids = "9";
        List<HashMap<String, Object>> actual = jsonResponseList(Stream.of(transactions3.get(1))
                .collect(Collectors.toList()));
        HashMap<String, Object> response = new HashMap<>();
        response.put("new_income_total", 500.00);
        response.put("updated_incomes", actual);
        response.put("total_incomes_updated", 1);
        response.put("invalid_ids", new ArrayList<>());
        response.put("total_invalid_ids", 0);

        when(transactionService.receiveIncomes(ids)).thenReturn(response);
        assertEquals(200, transactionController.receiveIncomes(ids).getStatusCodeValue());
    }

    @Test
    void shouldNOTReceiveIncomes() {
        String ids = "3,4";
        when(transactionService.receiveIncomes(ids)).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.receiveIncomes(ids).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.receiveIncomes(ids).getBody().get("message"));
    }

    @Test
    void shouldGetAllExpensesInCurrentBudget() {
        when(transactionService.getAllExpensesInCurrentBudget(auth)).thenReturn(new ArrayList<>());
        assertEquals(200, transactionController.getAllExpensesInCurrentBudget(auth).getStatusCodeValue());
    }

    @Test
    void shouldNOTGetAllExpensesInCurrentBudget() {
        when(transactionService.getAllExpensesInCurrentBudget(auth)).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.getAllExpensesInCurrentBudget(auth).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.getAllExpensesInCurrentBudget(auth).getBody().get("message"));
    }

    @Test
    void shouldGetAllIncomeInCurrentBudget() {
        when(transactionService.getAllIncomeInCurrentBudget(auth)).thenReturn(new HashMap<>());
        assertEquals(200, transactionController.getAllIncomeInCurrentBudget(auth).getStatusCodeValue());
    }

    @Test
    void shouldNOTGetAllIncomeInCurrentBudget() {
        when(transactionService.getAllIncomeInCurrentBudget(auth)).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.getAllIncomeInCurrentBudget(auth).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.getAllIncomeInCurrentBudget(auth).getBody().get("message"));
    }

    @Test
    void shouldGetUserPayments() {
        when(transactionService.getUserPayments(auth)).thenReturn(new HashMap<>());
        assertEquals(200, transactionController.getUserPayments(auth).getStatusCodeValue());
    }

    @Test
    void shouldNOTGetUserPayments() {
        when(transactionService.getUserPayments(auth)).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.getUserPayments(auth).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.getUserPayments(auth).getBody().get("message"));
    }

    @Test
    void shouldCreateNewTransaction() {
        when(transactionService.newTransaction(auth, new HashMap<>())).thenReturn(300.00);
        assertEquals(200, transactionController.newTransaction(auth, new HashMap<>()).getStatusCodeValue());
    }

    @Test
    void shouldNOTCreateNewTransaction() {
        when(transactionService.newTransaction(auth, new HashMap<>())).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.newTransaction(auth, new HashMap<>()).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.newTransaction(auth, new HashMap<>()).getBody().get("message"));
    }

    @Test
    void shouldRemoveTransaction() {
        when(transactionService.removeTransaction(1L)).thenReturn(300.00);
        assertEquals(200, transactionController.removeTransaction(1L).getStatusCodeValue());
    }

    @Test
    void shouldNOTRemoveTransaction() {
        when(transactionService.removeTransaction(1L)).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.removeTransaction(1L).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.removeTransaction(1L).getBody());
    }

    @Test
    void shouldUpdateTransaction() {
        when(transactionService.updateTransaction(1L, new HashMap<>())).thenReturn(300.00);
        assertEquals(200, transactionController.updateTransaction(1L, new HashMap<>()).getStatusCodeValue());
    }

    @Test
    void shouldNOTUpdateTransaction() {
        when(transactionService.updateTransaction(1L, new HashMap<>())).thenThrow(new NullPointerException("No IDs given"));
        assertEquals(400, transactionController.updateTransaction(1L, new HashMap<>()).getStatusCodeValue());
        assertEquals("No IDs given", transactionController.updateTransaction(1L, new HashMap<>()).getBody().get("message"));
    }
}