package com.jnk2016.soulyyoubackend.transaction;

import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(ApplicationUser user);
    List<Transaction> findByBudget(MonthlyBudget budget);
    List<Transaction> findByBudgetAndCompletedAndTypeOrderByTimestampDesc(MonthlyBudget monthlyBudget, boolean completed, String type);
    List<Transaction> findByBudgetAndCompletedAndTypeNotOrderByTimestampDesc(MonthlyBudget budget, boolean completed, String doNotIncludeType);
    List<Transaction> findByBudgetAndTypeAndCompletedAndTimestampAfterOrderByTimestampAsc(MonthlyBudget currentBudget, String type, boolean completed, LocalDateTime currentDateTime);
    List<Transaction> findByBudgetAndTypeAndCompletedOrderByTimestampAsc(MonthlyBudget currentBudget, String type, boolean completed);
    List<Transaction> findByUserAndTypeAndCompletedAndTimestampBeforeOrderByTimestampAsc(ApplicationUser user, String type, boolean completed, LocalDateTime currentDateTime);
}
