package com.jnk2016.soulyyoubackend.mood;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import com.jnk2016.soulyyoubackend.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoodServiceTest {
    @InjectMocks
    MoodService moodService;

    @Mock
    MoodRepository moodRepository;
    @Mock
    UserService userService;

    Authentication auth;

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
        long i = 1L;

        moods1.add(new Mood(i++,
                0,
                "Messed up on test",
                LocalDateTime.now().plusHours(8),
                user1
        ));
        moods1.add(new Mood(i++,
                1,
                "Satisfied with my portfolio",
                LocalDateTime.now().minusDays(2).plusHours(2),
                user1
        ));
        moods1.add(new Mood(i++,
                2,
                "Went to sleep a bit mad",
                LocalDateTime.now().minusDays(4).plusHours(6),
                user1
        ));
        moods1.add(new Mood(i++,
                5,
                "Got some tax returns",
                LocalDateTime.now().minusDays(5),
                user1
        ));
        moods1.add(new Mood(i++,
                8,
                "Package went missing",
                LocalDateTime.now().minusDays(10),
                user1
        ));

        moods2.add(new Mood(i++,
                3,
                "Stomach has been very upset",
                LocalDateTime.now().minusDays(20),
                user2
        ));
        moods2.add(new Mood(i++,
                6,
                "Unisom makes me drowsy",
                LocalDateTime.now().minusDays(1).plusHours(9),
                user2
        ));
        moods2.add(new Mood(i++,
                4,
                "Finally finished the budget tracker",
                LocalDateTime.now().minusDays(4).plusHours(4),
                user2
        ));
        moods2.add(new Mood(i,
                7,
                "Found out new methods for unit tests",
                LocalDateTime.now().plusHours(4),
                user2
        ));

        user1.setMoods(moods1);
        user2.setMoods(moods2);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializeUsers();
        initializeMoods();
        auth = mock(Authentication.class);
    }

    @Test
    void shouldCreateNewMood() {
        Mood actual = new Mood(
                10L,
                8,
                "About to finish mood tracker ;)",
                LocalDateTime.of(2021, 6, 6, 21, 11, 30),
                user1
        );

        HashMap<String, Object> body = new HashMap<>();
        body.put("feeling", 8);
        body.put("note", "About to finish mood tracker ;)");
        body.put("timestamp", LocalDateTime.parse("2021-06-06T21:11:30"));
        when(userService.getApplicationUser(auth))
                .thenReturn(user1);

        Mood result = moodService.createNewMood(auth, body);
        assertEquals(actual.getFeeling(), result.getFeeling());
        assertEquals(actual.getNote(), result.getNote());
        assertEquals(actual.getTimestamp(), result.getTimestamp());
        assertEquals(actual.getUser().getUserId(), result.getUser().getUserId());
    }

    @Test
    void shouldThrowEntityNotFoundExcWhenCreateNewMood() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("feeling", 8);
        body.put("note", "About to finish mood tracker ;)");
        body.put("timestamp", LocalDateTime.parse("2021-06-06T21:11:30"));
        when(userService.getApplicationUser(auth))
                .thenThrow(new EntityNotFoundException());

        assertThrows(EntityNotFoundException.class,
                ()-> moodService.createNewMood(auth, body));
    }

    @Test
    void shouldThrowIllegalArgExcWhenCreateNewMood() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("feeling", 8);
        body.put("note", "About to finish mood tracker ;)");
        body.put("timestamp", LocalDateTime.parse("2021-06-06T21:11:30"));
        when(userService.getApplicationUser(auth))
                .thenReturn(user1);

        body.put("mood_id", 10L);
        Exception thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.createNewMood(auth, body));
        assertEquals("Incorrect formatting of request body", thrown.getMessage());
        body.remove("mood_id");

        body.remove("feeling");
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.createNewMood(auth, body));
        assertEquals("Incorrect formatting of request body", thrown.getMessage());

        body.put("feeling", 9);
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.createNewMood(auth, body));
        assertEquals("Invalid feeling integer value", thrown.getMessage());

        body.put("feeling", -1);
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.createNewMood(auth, body));
        assertEquals("Invalid feeling integer value", thrown.getMessage());
    }

    @Test
    void shouldGetLatestMoodEntry() {
        when(userService.getApplicationUser(auth))
                .thenReturn(user1);
        when(moodRepository.findFirstByUserOrderByTimestampDesc(user1))
                .thenReturn(moods1.get(0));

        assertEquals(moods1.get(0), moodService.getLatestMoodEntry(auth));
    }

    @Test
    void shouldThrowEntityNotFoundExcWhenGetLatestMoodEntry() {
        when(userService.getApplicationUser(auth))
                .thenThrow(new EntityNotFoundException());

        assertThrows(EntityNotFoundException.class,
                ()-> moodService.getLatestMoodEntry(auth));
    }

    @Test
    void shouldReturnNullWhenGetLatestMoodEntry() {
        when(userService.getApplicationUser(auth))
                .thenReturn(user1);
        when(moodRepository.findFirstByUserOrderByTimestampDesc(user1))
                .thenReturn(null);

        assertNull(moodService.getLatestMoodEntry(auth));
    }

    @Test
    void shouldGetAllMoodsThisMonth() {
        when(userService.getApplicationUser(auth))
                .thenReturn(user1);

        LocalDateTime date1 = LocalDateTime.of(
                2021,
                6,
                1,
                0,
                0,
                0,
                0
        );
        LocalDateTime date2 = LocalDateTime.of(
                2021,
                6,
                30,
                23,
                59,
                59,
                0
        );
        when(moodRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(user1, date1, date2))
                .thenReturn(moods1);

        assertEquals(moods1.get(0).getFeeling(), moodService.getAllMoodsThisMonth(auth).get(0).getFeeling());
    }

    @Test
    void shouldUpdateMood() {
        Mood actual = new Mood(
                1L,
                8,
                "About to finish mood tracker ;)",
                LocalDateTime.of(2021, 6, 6, 21, 11, 30),
                user1
        );

        HashMap<String, Object> body = new HashMap<>();
        body.put("feeling", 8);
        body.put("note", "About to finish mood tracker ;)");
        body.put("timestamp", LocalDateTime.parse("2021-06-06T21:11:30"));

        when(moodRepository.findById(1L))
                .thenReturn(Optional.of(moods1.get(0)));

        Mood result = moodService.updateMood(1L, body);
        assertEquals(actual.getMoodId(), result.getMoodId());
        assertEquals(actual.getFeeling(), result.getFeeling());
        assertEquals(actual.getNote(), result.getNote());
        assertEquals(actual.getTimestamp(), result.getTimestamp());
        assertEquals(actual.getUser().getUserId(), result.getUser().getUserId());
    }

    @Test
    void shouldThrowNullPointerExcWhenUpdateMood() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("feeling", 8);
        body.put("note", "About to finish mood tracker ;)");
        body.put("timestamp", LocalDateTime.parse("2021-06-06T21:11:30"));

        when(moodRepository.findById(1L))
                .thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class,
                ()-> moodService.updateMood(1L, body));
    }

    @Test
    void shouldThrowIllegalArgExcWhenUpdateMood() {
        when(moodRepository.findById(1L))
                .thenReturn(Optional.of(moods1.get(0)));

        HashMap<String, Object> body = new HashMap<>();

        body.put("feelings", 8);
        Exception thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.updateMood(1L, body));
        assertEquals("Please provide a correct attribute to update", thrown.getMessage());
        body.remove("feelings");

        thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.updateMood(1L, body));
        assertEquals("Please provide a correct attribute to update", thrown.getMessage());

        body.put("feeling", 9);
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.updateMood(1L, body));
        assertEquals("Invalid feeling integer value", thrown.getMessage());

        body.put("feeling", -1);
        thrown = assertThrows(IllegalArgumentException.class,
                ()-> moodService.updateMood(1L, body));
        assertEquals("Invalid feeling integer value", thrown.getMessage());
    }
}