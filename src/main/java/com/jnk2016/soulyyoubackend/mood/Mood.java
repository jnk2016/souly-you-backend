package com.jnk2016.soulyyoubackend.mood;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="mood")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="mood_id")
    private long moodId;

    private int feeling;    // Choose from feelings 0 to 8...
    @Column(columnDefinition = "TEXT")
    private String note;
    private LocalDateTime timestamp;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private ApplicationUser user;

    public Mood(int feeling, String note, LocalDateTime timestamp, ApplicationUser user) {
        this.feeling = feeling;
        this.note = note;
        this.timestamp = timestamp;
        this.user = user;
    }
}
