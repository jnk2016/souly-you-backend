package com.jnk2016.soulyyoubackend.monthlybudget;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    List<MonthlyBudget> findByUser(ApplicationUser user);
    MonthlyBudget findByUserAndMonthAndYear(ApplicationUser user, int month, int year);
    MonthlyBudget findFirstByUserOrderByBudgetIdDesc(ApplicationUser user);
}
