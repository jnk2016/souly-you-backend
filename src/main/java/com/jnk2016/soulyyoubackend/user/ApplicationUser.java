package com.jnk2016.soulyyoubackend.user;
import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.mood.Mood;
import com.jnk2016.soulyyoubackend.savingsgoal.SavingsGoal;
import com.jnk2016.soulyyoubackend.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Intellij shortcuts:  Help | Tip of the Day
 *
 * ctrl + shift + (up or down) = shift code fragment up/down a line
 *
 * (hold) (shift + alt) = click on and select multiple text fragments to modify
 *
 * ctrl + d = duplicate the selected block or current line when no block selected
 *
 * (drag mouse) alt = select a rectangular piece of code
 *
 * Use Refactor | Copy to create a class which is a copy of the selected class.
 * This is useful when you need to create a class similar to an existing one, and it's not feasible to put shared functionality in a common superclass.
 */
@Entity
@Table(name="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private long userId;

    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private LocalDate dateJoined;

    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<MonthlyBudget> budgets;


    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Transaction> transactions;

    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<SavingsGoal> savingsGoals;

    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Mood> moods;

    public ApplicationUser(String username, String password, String firstname, String lastname, LocalDate dateJoined){
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dateJoined = dateJoined;
    }
}