package com.jnk2016.soulyyoubackend.savingsgoal;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name="savings_goal")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="savings_id")
    private long savingsId;

    private String name;
    private double goalAmount;
    private double savedAmount;
    private boolean complete;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private ApplicationUser user;
}
