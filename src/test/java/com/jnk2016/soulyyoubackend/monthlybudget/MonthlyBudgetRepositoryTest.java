package com.jnk2016.soulyyoubackend.monthlybudget;

import com.jnk2016.soulyyoubackend.transaction.Transaction;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MonthlyBudgetRepositoryTest {
    @Autowired
    MonthlyBudgetRepository monthlyBudgetRepository;

    ApplicationUser user1;
    ApplicationUser user2;

    MonthlyBudget monthlyBudget1;
    MonthlyBudget monthlyBudget2;
    MonthlyBudget monthlyBudget3;

    void addData() {
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        addData();
    }

    @Test
    void shouldFindByUserAndMonthAndYear() {
        assertEquals(monthlyBudget1, monthlyBudgetRepository.findByUserAndMonthAndYear(user1, 3,2021));
    }

    @Test
    void shouldNotFindByUserAndMonthAndYear() {
        assertNull(monthlyBudgetRepository.findByUserAndMonthAndYear(user1, 2, 2021));
    }

    @Test
    void shouldFindFirstByUserOrderByBudgetIdDesc() {
        assertEquals(monthlyBudget2, monthlyBudgetRepository.findFirstByUserOrderByBudgetIdDesc(user1));
    }

    @Test
    void shouldNotFindFirstByUserOrderByBudgetIdDesc() {
        ApplicationUser user = user2;
        user.setUsername("dp");
        user.setUserId(4);
        assertNull(monthlyBudgetRepository.findFirstByUserOrderByBudgetIdDesc(user));
    }

    @Test
    void shouldFindById() {
        assertEquals(monthlyBudget3, monthlyBudgetRepository.findById(3L).orElse(null));
    }

    @Test
    void findByIdShouldNOTFindByIdWhenInvalidId() {
        assertThrows(
                NullPointerException.class, ()-> monthlyBudgetRepository.findById(4L).orElseThrow(()->new NullPointerException("Budget not found")));
    }
}