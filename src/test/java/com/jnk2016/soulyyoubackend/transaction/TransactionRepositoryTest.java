package com.jnk2016.soulyyoubackend.transaction;

import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudgetRepository;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    MonthlyBudgetRepository monthlyBudgetRepository;

    ApplicationUser user1;
    ApplicationUser user2;

    MonthlyBudget monthlyBudget1;
    MonthlyBudget monthlyBudget2;
    MonthlyBudget monthlyBudget3;

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
        monthlyBudgetRepository.save(monthlyBudget1);

        monthlyBudget2.setUser(user1);
        monthlyBudget2.setMonth(4);
        monthlyBudget2.setYear(2021);
        monthlyBudget2.setBudgetGoal(2500);
        monthlyBudgetRepository.save(monthlyBudget2);

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
        monthlyBudgetRepository.save(monthlyBudget3);
    }

    void initializeTransactions() {
        transactions1 = new ArrayList<>();
        transactions2 = new ArrayList<>();
        transactions3 = new ArrayList<>();

        transactions1.add(new Transaction(60, "payment", false, LocalDateTime.now().minusDays(8), monthlyBudget1, user1));
        transactions1.add(new Transaction(80, "payment", true, LocalDateTime.now().minusDays(8), monthlyBudget1, user1));
        transactions1.add(new Transaction(500, "expense", true, LocalDateTime.now().plusDays(5), monthlyBudget1, user1));
        transactions1.add(new Transaction(100, "income", true, LocalDateTime.now().minusDays(10), monthlyBudget1, user1));
        transactions1.add(new Transaction(50, "income", false, LocalDateTime.now().minusDays(20), monthlyBudget1, user1));
        for(Transaction transaction : transactions1){
            transactionRepository.save(transaction);
        }

        transactions2.add(new Transaction(100, "payment", false, LocalDateTime.now().minusDays(5), monthlyBudget2, user1));
        transactions2.add(new Transaction(600, "payment", false, LocalDateTime.now().plusDays(5), monthlyBudget2, user1));
        transactions2.add(new Transaction(100, "payment", false, LocalDateTime.now().plusDays(6), monthlyBudget2, user1));
        transactions2.add(new Transaction(70, "payment", true, LocalDateTime.now().plusDays(6), monthlyBudget2, user1));
        transactions2.add(new Transaction(25, "payment", false, LocalDateTime.now().plusDays(10), monthlyBudget2, user1));
        transactions2.add(new Transaction(50, "expense", true, LocalDateTime.now().plusDays(20), monthlyBudget2, user1));
        transactions2.add(new Transaction(25, "expense", true, LocalDateTime.now().plusDays(4), monthlyBudget2, user1));
        transactions2.add(new Transaction(500, "income", true, LocalDateTime.now().minusDays(21), monthlyBudget2, user1));
        for(Transaction transaction : transactions2){
            transactionRepository.save(transaction);
        }

        transactions3.add(new Transaction(6400, "income", true, LocalDateTime.now().minusDays(3), monthlyBudget3, user2));
        for(Transaction transaction : transactions3){
            transactionRepository.save(transaction);
        }
    }

    @BeforeEach
    void setUp() {
        initializeBudgetsAndUsers();
        initializeTransactions();
    }

    @Test
    void shouldFindByBudgetAndCompletedAndTwoTypes() {
        List<Transaction> results = transactionRepository.findByBudgetAndCompletedAndTypeNotOrderByTimestampDesc(monthlyBudget1, true, "income");
        assertEquals(transactions1.get(1), results.get(1));
        assertEquals(transactions1.get(2), results.get(0));
    }

    @Test
    void shouldFindByBudgetAndType() {
        List<Transaction> results = transactionRepository.findByBudgetAndCompletedAndTypeOrderByTimestampDesc(monthlyBudget1, true, "income");
        assertEquals(transactions1.get(3), results.get(0));
    }

    @Test
    void shouldGetThisMonthUpcomingUnpaidExpenses() {
        List<Transaction> results = transactionRepository.findByBudgetAndTypeAndCompletedAndTimestampAfterOrderByTimestampAsc(
                monthlyBudget2, "expense", false, LocalDateTime.now());
        for(Transaction entry : results) {
            assertEquals("expense", entry.getType());
            assertFalse(entry.isCompleted());
            assertTrue(entry.getTimestamp().isAfter(LocalDateTime.now()));
        }
    }

    @Test
    void shouldGetOverdueExpenses() {
        List<Transaction> results = transactionRepository.findByUserAndTypeAndCompletedAndTimestampBeforeOrderByTimestampAsc(
                user1, "expense", false, LocalDateTime.now());
        assertEquals(transactions1.get(0), results.get(0));
        assertEquals(transactions2.get(0), results.get(1));
        assertEquals(2, results.size());
    }

    @Test
    void shouldGetThisMonthPaidExpenses() {
        List<Transaction> results = transactionRepository.findByBudgetAndTypeAndCompletedOrderByTimestampAsc(monthlyBudget2, "expense", true);
        for(Transaction transaction : results) {
            assertEquals("expense", transaction.getType());
            assertTrue(transaction.isCompleted());
            assertEquals(user1, transaction.getUser());
            System.out.println(transaction.getTimestamp());
        }
    }

}