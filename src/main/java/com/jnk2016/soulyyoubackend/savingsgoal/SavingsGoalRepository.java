package com.jnk2016.soulyyoubackend.savingsgoal;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(ApplicationUser user);
}



