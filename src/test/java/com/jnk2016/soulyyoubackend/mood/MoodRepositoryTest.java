package com.jnk2016.soulyyoubackend.mood;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import com.jnk2016.soulyyoubackend.user.ApplicationUserRepository;
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
class MoodRepositoryTest {
    @Autowired
    MoodRepository moodRepository;

    BCryptPasswordEncoder bCryptPasswordEncoder;

    ApplicationUser user1;
    ApplicationUser user2;

    List<Mood> moods1;
    List<Mood> moods2;

    void initializeUsers() {
        user1 = new ApplicationUser();
        user2 = new ApplicationUser();

        bCryptPasswordEncoder = new BCryptPasswordEncoder();

        user1.setUserId(1);
        user1.setUsername("dp2021");
        user1.setFirstname("Danphuong");
        user1.setLastname("Hoang");
        user1.setPassword(bCryptPasswordEncoder.encode("Password"));
        user1.setDateJoined(LocalDate.now());

        user2.setUserId(2);
        user2.setUsername("jaxnk2020");
        user2.setFirstname("Jackson");
        user2.setLastname("Kim");
        user2.setPassword(bCryptPasswordEncoder.encode("Password"));
        user2.setDateJoined(LocalDate.now());
    }

    void initializeMoods() {
        moods1 = new ArrayList<>();
        moods2 = new ArrayList<>();

        moods1.add(new Mood(
                0,
                "Messed up on test",
                LocalDateTime.now().plusHours(8),
                user1
        ));
        moods1.add(new Mood(
                1,
                "Satisfied with my portfolio",
                LocalDateTime.now().minusDays(2).plusHours(2),
                user1
        ));
        moods1.add(new Mood(
                2,
                "Went to sleep a bit mad",
                LocalDateTime.now().minusDays(4).plusHours(6),
                user1
        ));
        moods1.add(new Mood(
                5,
                "Got some tax returns",
                LocalDateTime.now().minusDays(5),
                user1
        ));
        moods1.add(new Mood(
                8,
                "Package went missing",
                LocalDateTime.now().minusDays(10),
                user1
        ));
        
        moods2.add(new Mood(
                3,
                "Stomach has been very upset",
                LocalDateTime.now().minusDays(20),
                user2
        ));
        moods2.add(new Mood(
                6,
                "Unisom makes me drowsy",
                LocalDateTime.now().minusDays(1).plusHours(9),
                user2
        ));
        moods2.add(new Mood(
                4,
                "Finally finished the budget tracker",
                LocalDateTime.now().minusDays(4).plusHours(4),
                user2
        ));
        moods2.add(new Mood(
                7,
                "Found out new methods for unit tests",
                LocalDateTime.now().plusHours(4),
                user2
        ));

        for(Mood mood : moods1) {
            moodRepository.save(mood);
        }
        for(Mood mood : moods2) {
            moodRepository.save(mood);
        }
    }

    @BeforeEach
    void setUp() {
        initializeUsers();
        initializeMoods();
    }

    @Test
    void shouldFindByTimestampBetween() {
        List<Mood> actual = moods1;
        actual.remove(4);

        LocalDateTime date1 = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withDayOfMonth(1)
                .withMonth(LocalDate.now().getMonthValue())
                .withYear(LocalDate.now().getYear());
        LocalDateTime date2 = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withDayOfMonth(1)
                .withMonth(LocalDate.now().plusMonths(1).getMonthValue())
                .withYear(LocalDate.now().getYear())
                .minusDays(1);

        assertEquals(actual, moodRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(user1, date1, date2));
    }

    @Test
    void shouldFindFirstByUserOrderByTimestampDesc() {
        assertEquals(moods1.get(0), moodRepository.findFirstByUserOrderByTimestampDesc(user1));
        assertEquals(moods2.get(3), moodRepository.findFirstByUserOrderByTimestampDesc(user2));
    }
}