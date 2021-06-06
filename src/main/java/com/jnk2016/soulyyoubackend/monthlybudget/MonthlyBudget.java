package com.jnk2016.soulyyoubackend.monthlybudget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnk2016.soulyyoubackend.transaction.Transaction;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name="budget")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyBudget implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="budget_id")
    private long budgetId;

    private double balance=0;           // Income minus expenses (net total)
    private double income=0;
    private double expenses=0;
    private double budgetGoal=0;        // Budget amount set by the user
    private double budgetRemaining=0;   // Budget goal minus expenses

    private int month;
    private int year;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private ApplicationUser user;

    @OneToMany(mappedBy="budget", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Transaction> transactions;


}
