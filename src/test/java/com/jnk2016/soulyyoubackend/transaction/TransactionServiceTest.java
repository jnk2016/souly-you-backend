package com.jnk2016.soulyyoubackend.transaction;

import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudgetService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    @InjectMocks
    TransactionService transactionService;

    @Mock
    TransactionRepository transactionRepository;
    @Mock
    MonthlyBudgetService monthlyBudgetService;
    @Mock
    UserService userService;

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

    @Test
    void shouldConvertToJsonResponse() {
        List<HashMap<String, Object>> result = transactionService.toJsonBody(transactions2);
        for(HashMap<String, Object> response : result) {
            assertEquals(1L, response.get("user_id"));
            assertEquals(2L, response.get("budget_id"));
        }
    }

    @Test
    void shouldThrowNullWhenConvertToJsonResponse() {
        List<Transaction> transactions = new ArrayList<>();
        assertThrows(NullPointerException.class, ()-> transactionService.toJsonBody(transactions));
    }

    @Test
    void shouldGetAllExpensesInCurrentBudget() {
        List<String> filters = Stream.of("user", "budget")
                .collect(Collectors.toList());

        List<HashMap<String, Object>> response = new ArrayList<>();
        HashMap<String, Object> toJson = new HashMap<>();
        toJson.put("name", transactions1.get(0).getName());
        toJson.put("amount", transactions1.get(0).getAmount());
        toJson.put("type", transactions1.get(0).getType());
        toJson.put("completed", transactions1.get(0).isCompleted());
        toJson.put("timestamp", transactions1.get(0).getTimestamp());

        transactions1.remove(1);

        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget1);
        when(transactionRepository
                .findByBudgetAndCompletedAndTypeNotOrderByTimestampDesc(
                        monthlyBudget1, true, "income"))
                .thenReturn(transactions1);
        when(userService.toJsonBody(Transaction.class, transactions1.get(0), filters)).thenReturn(toJson);

        toJson.put("user_id", transactions1.get(0).getUser().getUserId());
        toJson.put("budget_id", transactions1.get(0).getBudget().getBudgetId());
        response.add(toJson);

        assertEquals(response, transactionService.getAllExpensesInCurrentBudget(auth));
    }

    @Test
    void shouldReturnEmptyExpenseListWhenGetAllExpensesInCurrent() {
        List<String> filters = Stream.of("user", "budget")
                .collect(Collectors.toList());

        List<HashMap<String, Object>> response = new ArrayList<>();
        HashMap<String, Object> toJson = new HashMap<>();
        toJson.put("name", transactions1.get(0).getName());
        toJson.put("amount", transactions1.get(0).getAmount());
        toJson.put("type", transactions1.get(0).getType());
        toJson.put("completed", transactions1.get(0).isCompleted());
        toJson.put("timestamp", transactions1.get(0).getTimestamp());

        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget1);
        when(transactionRepository
                .findByBudgetAndCompletedAndTypeNotOrderByTimestampDesc(
                        monthlyBudget1, true, "income"))
                .thenReturn(new ArrayList<>());

        assertEquals(new ArrayList<>(), transactionService.getAllExpensesInCurrentBudget(auth));
    }

    @Test
    void shouldNOTGetAllExpensesInCurrentBudgetWhenInvalidBudget() {
        when(monthlyBudgetService.getCurrentBudget(auth)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, ()-> transactionService.getAllExpensesInCurrentBudget(auth));
    }

    @Test
    void shouldGetAllIncomeInCurrentBudget() {
        List<String> filters = Stream.of("user", "budget")
                .collect(Collectors.toList());

        HashMap<String, Object> response = new HashMap<>();
        for(Transaction transaction : transactions3) {
            List<HashMap<String, Object>> jsonList = new ArrayList<>();
            HashMap<String, Object> toJson = new HashMap<>();
            toJson.put("name", transaction.getName());
            toJson.put("amount", transaction.getAmount());
            toJson.put("type", transaction.getType());
            toJson.put("completed", transaction.isCompleted());
            toJson.put("timestamp", transaction.getTimestamp());
            when(userService.toJsonBody(Transaction.class, transaction, filters)).thenReturn(toJson);
            jsonList.add(toJson);

            toJson.put("user_id", transaction.getUser().getUserId());
            toJson.put("budget_id", transaction.getBudget().getBudgetId());
            response.put((boolean)toJson.get("completed") ? "received_incomes" : "pending_incomes", jsonList);
        }

        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget3);

        List<Transaction> transactions3Clone = new ArrayList<>();
        transactions3Clone.add(transactions3.get(1));
        transactions3.remove(1);

        when(transactionRepository
                .findByBudgetAndCompletedAndTypeOrderByTimestampDesc(
                        monthlyBudget3, true, "income"))
                .thenReturn(transactions3);
        when(transactionRepository
                .findByBudgetAndCompletedAndTypeOrderByTimestampDesc(
                        monthlyBudget3, false, "income"))
                .thenReturn(transactions3Clone);

        assertEquals(response, transactionService.getAllIncomeInCurrentBudget(auth));
    }

    @Test
    void shouldNOTGetAllIncomesInCurrentBudgetWhenInvalidBudget() {
        when(monthlyBudgetService.getCurrentBudget(auth)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, ()-> transactionService.getAllIncomeInCurrentBudget(auth));
    }

    @Test
    void shouldGetAllUserPayments() {
        List<Transaction> completed = new ArrayList<>();
        completed.add(transactions2.get(0));
        completed.add(transactions2.get(1));
        List<Transaction> upcoming = new ArrayList<>();
        upcoming.add(transactions2.get(2));
        upcoming.add(transactions2.get(3));
        List<Transaction> overdue = new ArrayList<>();
        overdue.add(transactions1.get(1));


        List<List<Transaction>> transactionLists = Stream.of(completed,upcoming,overdue)
                .collect(Collectors.toList());

        List<String> filters = Stream.of("user", "budget")
                .collect(Collectors.toList());

        List<List<HashMap<String, Object>>> results = new ArrayList<>();
        for(List<Transaction> list : transactionLists) {
            List<HashMap<String, Object>> jsonList = new ArrayList<>();
            for(Transaction transaction : list) {
                HashMap<String, Object> toJson = new HashMap<>();
                toJson.put("name", transaction.getName());
                toJson.put("amount", transaction.getAmount());
                toJson.put("type", transaction.getType());
                toJson.put("completed", transaction.isCompleted());
                toJson.put("timestamp", transaction.getTimestamp());
                when(userService.toJsonBody(Transaction.class, transaction, filters)).thenReturn(toJson);
                jsonList.add(toJson);

                toJson.put("user_id", transaction.getUser().getUserId());
                toJson.put("budget_id", transaction.getBudget().getBudgetId());
            }
            results.add(jsonList);
        }

        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(transactionRepository.findByBudgetAndTypeAndCompletedOrderByTimestampAsc(monthlyBudget2, "payment", true))
                .thenReturn(completed);
        when(transactionRepository.findByBudgetAndTypeAndCompletedAndTimestampAfterOrderByTimestampAsc(eq(monthlyBudget2), eq("payment"), eq(false), any(LocalDateTime.class)))
                .thenReturn(upcoming);
        when(transactionRepository.findByUserAndTypeAndCompletedAndTimestampBeforeOrderByTimestampAsc(eq(user1), eq("payment"), eq(false), any(LocalDateTime.class)))
                .thenReturn(overdue);

        HashMap<String, Object> result = transactionService.getUserPayments(auth);
        assertEquals(5, (int)result.get("total_payments"));
        assertEquals(2, (int)((HashMap<String,Object>) result.get("pending_payments")).get("total"));
        assertEquals(2, (int)((HashMap<String,Object>) result.get("completed_payments")).get("total"));
        assertEquals(1, (int)((HashMap<String,Object>) result.get("overdue_payments")).get("total"));
    }

    @Test
    void shouldReturnNullItemsWhenGetUserPayments() {
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(transactionRepository.findByBudgetAndTypeAndCompletedOrderByTimestampAsc(monthlyBudget2, "payment", true))
                .thenReturn(null);
        when(transactionRepository.findByBudgetAndTypeAndCompletedAndTimestampAfterOrderByTimestampAsc(eq(monthlyBudget2), eq("payment"), eq(false), any(LocalDateTime.class)))
                .thenReturn(null);
        when(transactionRepository.findByUserAndTypeAndCompletedAndTimestampBeforeOrderByTimestampAsc(eq(user1), eq("payment"), eq(false), any(LocalDateTime.class)))
                .thenReturn(null);

        HashMap<String, Object> result = transactionService.getUserPayments(auth);

        assertEquals(0, (int)result.get("total_payments"));
        assertEquals(0, (int)((HashMap<String,Object>) result.get("pending_payments")).get("total"));
        assertEquals(0, (int)((HashMap<String,Object>) result.get("completed_payments")).get("total"));
        assertEquals(0, (int)((HashMap<String,Object>) result.get("overdue_payments")).get("total"));
        assertEquals(new ArrayList<>(), ((HashMap<String, Object>) result.get("pending_payments")).get("items"));
        assertEquals(new ArrayList<>(), ((HashMap<String, Object>) result.get("completed_payments")).get("items"));
        assertEquals(new ArrayList<>(), ((HashMap<String, Object>) result.get("overdue_payments")).get("items"));
    }

    @Test
    void whenNewIncomeShouldReturnUpdatedIncomeTotal() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("name", "work");
        body.put("amount", 800.00);
        body.put("completed", true);
        body.put("type", "income");
        body.put("timestamp", LocalDateTime.now().minusDays(10));
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        when(monthlyBudgetService.updateIncome(monthlyBudget2, 800)).thenReturn(1300.00);
        assertEquals(1300.00, transactionService.newTransaction(auth, body));
    }

    @Test
    void whenNewIncomeShouldReturnSameUpdatedIncomeTotal() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("name", "work");
        body.put("amount", 800.00);
        body.put("completed", false);
        body.put("type", "income");
        body.put("timestamp", LocalDateTime.now().minusDays(10));
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        assertEquals(500.00, transactionService.newTransaction(auth, body));
    }

    @Test
    void whenNewExpenseShouldReturnUpdatedExpense() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("name", "groceries");
        body.put("amount", 30.00);
        body.put("completed", true);
        body.put("type", "expense");
        body.put("timestamp", LocalDateTime.now().minusDays(4));
        Authentication auth = mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 30)).thenReturn(900.00);
        assertEquals(900.00, transactionService.newTransaction(auth, body));
    }

    @Test
    void whenNewPaymentShouldReturnUpdatedExpense() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("name", "groceries");
        body.put("amount", 30.00);
        body.put("completed", true);
        body.put("type", "payment");
        body.put("timestamp", LocalDateTime.now().minusDays(4));
        Authentication auth = mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 30)).thenReturn(900.00);
        assertEquals(900.00, transactionService.newTransaction(auth, body));
    }

    @Test
    void whenNewPaymentShouldReturnSameExpense() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("name", "groceries");
        body.put("amount", 30.00);
        body.put("completed", false);
        body.put("type", "payment");
        body.put("timestamp", LocalDateTime.now().minusDays(4));
        Authentication auth = mock(Authentication.class);
        when(userService.getApplicationUser(auth)).thenReturn(user1);
        when(monthlyBudgetService.getCurrentBudget(auth)).thenReturn(monthlyBudget2);
        assertEquals(170.00, transactionService.newTransaction(auth, body));
    }

    @Test
    void shouldRemoveReceivedIncomeTransaction() {
        when(transactionRepository.findById(9L)).thenReturn(Optional.of(transactions2.get(6)));
        when(monthlyBudgetService.updateIncome(monthlyBudget2, -500.00)).thenReturn(0.00);
        assertEquals(0.00, transactionService.removeTransaction(9L));
    }

    @Test
    void shouldRemovePendingIncomeTransaction() {
        Transaction transaction9 = transactions2.get(6);
        transaction9.setCompleted(false);
        transactions2.set(6, transaction9);
        when(transactionRepository.findById(9L)).thenReturn(Optional.of(transactions2.get(6)));
        assertEquals(500.00, transactionService.removeTransaction(9L));
    }

    @Test
    void shouldRemoveExpenseTransaction() {
        when(transactionRepository.findById(8L)).thenReturn(Optional.of(transactions2.get(5)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, -25.00)).thenReturn(145.00);
        assertEquals(145.00, transactionService.removeTransaction(8L));
    }

    @Test
    void shouldRemoveCompletePaymentTransaction() {
        when(transactionRepository.findById(6L)).thenReturn(Optional.of(transactions2.get(3)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, -25.00)).thenReturn(145.00);
        assertEquals(145.00, transactionService.removeTransaction(6L));
    }

    @Test
    void shouldRemoveIncompletePaymentTransaction() {
        when(transactionRepository.findById(3L)).thenReturn(Optional.of(transactions2.get(0)));
        assertEquals(170.00, transactionService.removeTransaction(3L));
    }

    @Test
    void shouldThrowNullWhenRemoveTransaction() {
        when(transactionRepository.findById(14L)).thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class, ()-> transactionService.removeTransaction(14L));
    }

    public HashMap<String, Object> body() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("name", "walgreens");
        body.put("amount", 100.00);
        body.put("completed", true);
        body.put("timestamp", LocalDateTime.now());
        return body;
    }

    @Test
    void shouldUpdateReceivedIncomeTransaction() {
        HashMap<String, Object> body = body();
        when(transactionRepository.findById(9L)).thenReturn(Optional.of(transactions2.get(6)));
        when(monthlyBudgetService.updateIncome(monthlyBudget2, -400.00)).thenReturn(100.00);
        assertEquals(100.00, transactionService.updateTransaction(9L, body));
    }

    @Test
    void shouldUpdateToReceivedIncomeTransaction() {
        HashMap<String, Object> body = body();
        Transaction transaction9 = transactions2.get(6);
        transaction9.setCompleted(false);
        transactions2.set(6, transaction9);
        monthlyBudget2.setIncome(0.00);
        when(transactionRepository.findById(9L)).thenReturn(Optional.of(transactions2.get(6)));
        when(monthlyBudgetService.updateIncome(monthlyBudget2, 100.00)).thenReturn(100.00);
        assertEquals(100.00, transactionService.updateTransaction(9L, body));
    }

    @Test
    void shouldUpdateToPendingIncomeTransaction() {
        HashMap<String, Object> body = body();
        body.put("completed", false);
        when(transactionRepository.findById(9L)).thenReturn(Optional.of(transactions2.get(6)));
        when(monthlyBudgetService.updateIncome(monthlyBudget2, -500.00)).thenReturn(0.00);
        assertEquals(0.00, transactionService.updateTransaction(9L, body));
    }

    @Test
    void shouldUpdatePendingIncomeTransaction() {
        HashMap<String, Object> body = body();
        body.put("completed", false);
        Transaction transaction9 = transactions2.get(6);
        transaction9.setCompleted(false);
        transactions2.set(6, transaction9);
        when(transactionRepository.findById(9L)).thenReturn(Optional.of(transactions2.get(6)));
        assertEquals(500.00, transactionService.updateTransaction(9L, body));
    }

    @Test
    void shouldUpdateExpenseTransaction() {
        HashMap<String, Object> body = body();
        when(transactionRepository.findById(8L)).thenReturn(Optional.of(transactions2.get(5)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 75.00)).thenReturn(245.00);
        assertEquals(245.00, transactionService.updateTransaction(8L, body));
    }

    @Test
    void shouldUpdateCompletePaymentTransaction() {
        HashMap<String, Object> body = body();
        when(transactionRepository.findById(6L)).thenReturn(Optional.of(transactions2.get(3)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 75.00)).thenReturn(245.00);
        assertEquals(245.00, transactionService.updateTransaction(6L, body));
    }

    @Test
    void shouldUpdateToCompletePaymentTransaction() {
        HashMap<String, Object> body = body();
        when(transactionRepository.findById(3L)).thenReturn(Optional.of(transactions2.get(0)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 100.00)).thenReturn(270.00);
        assertEquals(270.00, transactionService.updateTransaction(3L, body));
    }

    @Test
    void shouldThrowNullWhenUpdateTransaction() {
        HashMap<String, Object> body = body();
        body.remove("name");
        assertThrows(IllegalArgumentException.class, ()-> transactionService.updateTransaction(14L, body));

        body.put("name", "walgreens");
        when(transactionRepository.findById(14L)).thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class, ()-> transactionService.updateTransaction(14L, body));
    }

    @Test
    void shouldCompletePaymentTransaction() {
        String pathParam = "3,4";
        String type = "payment";

        List<Transaction> actualUpdatedTransactions = Stream.of(transactions2.get(0),transactions2.get(1))
                .collect(Collectors.toList());
        List<HashMap<String,Object>> actualUpdatedJsonTransactions = transactionService.toJsonBody(actualUpdatedTransactions);

        when(transactionRepository.findById(3L)).thenReturn(Optional.of(transactions2.get(0)));
        when(transactionRepository.findById(4L)).thenReturn(Optional.of(transactions2.get(1)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 700.00)).thenReturn(870.00);

        HashMap<String, Object> response = transactionService.completeTransactions(pathParam, type);
        assertEquals(870.00, response.get("new_type_total"));
        assertEquals(actualUpdatedJsonTransactions, response.get("updated_transactions"));
        assertEquals(2, response.get("total_transactions_updated"));
        assertEquals(new ArrayList<>(), response.get("invalid_ids"));
        assertEquals(0, response.get("total_invalid_ids"));
    }

    @Test
    void shouldCompletePaymentTransactionWithInvalidIds() {
        String pathParam = "3,4,5,9,13,8";
        String type = "payment";

        List<Long> actualInvalidIds = Stream.of(transactions2.get(2).getTransactionId(),transactions2.get(6).getTransactionId()
                ,13L,transactions2.get(5).getTransactionId())
                .collect(Collectors.toList());

        List<Transaction> actualUpdatedTransactions = Stream.of(transactions2.get(0),transactions2.get(1))
                .collect(Collectors.toList());
        List<HashMap<String,Object>> actualUpdatedJsonTransactions = transactionService.toJsonBody(actualUpdatedTransactions);

        when(transactionRepository.findById(3L)).thenReturn(Optional.of(transactions2.get(0)));
        when(transactionRepository.findById(4L)).thenReturn(Optional.of(transactions2.get(1)));
        when(monthlyBudgetService.updateExpenses(monthlyBudget2, 700.00)).thenReturn(870.00);

        HashMap<String, Object> response = transactionService.completeTransactions(pathParam, type);
        assertEquals(870.00, response.get("new_type_total"));
        assertEquals(actualUpdatedJsonTransactions, response.get("updated_transactions"));
        assertEquals(2, response.get("total_transactions_updated"));
        assertEquals(actualInvalidIds, response.get("invalid_ids"));
        assertEquals(4, response.get("total_invalid_ids"));
    }

    @Test
    void shouldCompleteIncomeTransaction() {
        String pathParam = "11";
        String type = "income";

        List<Transaction> actualUpdatedTransactions = Stream.of(transactions3.get(1))
                .collect(Collectors.toList());
        List<HashMap<String,Object>> actualUpdatedJsonTransactions = transactionService.toJsonBody(actualUpdatedTransactions);

        when(transactionRepository.findById(11L)).thenReturn(Optional.of(transactions3.get(1)));
        when(monthlyBudgetService.updateIncome(monthlyBudget3, 3200.00)).thenReturn(9600.00);

        HashMap<String, Object> response = transactionService.completeTransactions(pathParam, type);
        assertEquals(9600.00, response.get("new_type_total"));
        assertEquals(actualUpdatedJsonTransactions, response.get("updated_transactions"));
        assertEquals(1, response.get("total_transactions_updated"));
        assertEquals(new ArrayList<>(), response.get("invalid_ids"));
        assertEquals(0, response.get("total_invalid_ids"));
    }

    @Test
    void shouldNOTCompleteIncomeTransactionWhenNoIdsGiven() {
        String pathParam = "";
        String type = "income";

        assertThrows(IllegalArgumentException.class, ()-> transactionService.completeTransactions(pathParam, type));
    }
}