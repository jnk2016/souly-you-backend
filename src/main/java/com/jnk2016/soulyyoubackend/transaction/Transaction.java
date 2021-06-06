package com.jnk2016.soulyyoubackend.transaction;

import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="transaction_id")
    private long transactionId;

    private String name = "";
    private double amount;
    private String type;    // "expense", "payment", or "income" (all completed payments are expenses...but all expenses aren't necessarily payments)
    private boolean completed;  // Applies to transactions of type "payment"

    private LocalDateTime timestamp;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="budget_id", nullable = false)
    private MonthlyBudget budget;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private ApplicationUser user;

    public Transaction(double amount, String type, boolean completed, LocalDateTime timestamp, MonthlyBudget budget, ApplicationUser user) {
        this.amount = amount;
        this.type = type;
        this.completed = completed;
        this.timestamp = timestamp;
        this.budget = budget;
        this.user = user;
    }
}
