package com.jnk2016.soulyyoubackend.mood;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import com.jnk2016.soulyyoubackend.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MoodService {
    @Autowired
    MoodRepository moodRepository;
    @Autowired
    UserService userService;

    public HashMap<String, Object> toJsonBody(Mood mood) {
        List<String> filters = Stream.of("user").collect(Collectors.toList());
        HashMap<String, Object> response = userService.toJsonBody(Mood.class, mood, filters);
        response.put("user_id", mood.getUser().getUserId());

        return response;
    }

    public Mood createNewMood(Authentication auth, HashMap<String, Object> body) {
        ApplicationUser user = userService.getApplicationUser(auth);

        if(!(body.containsKey("feeling") && body.containsKey("note") && body.containsKey("timestamp") && body.size() == 3)) {
            throw new IllegalArgumentException("Incorrect formatting of request body");
        }
        else if(!((int)body.get("feeling") >= 0 && (int)body.get("feeling") <= 8)) {
            throw new IllegalArgumentException("Invalid feeling integer value");
        }

        Mood newEntry = new Mood(
                (int)body.get("feeling"),
                (String)body.get("note"),
                (LocalDateTime)body.get("timestamp"),
                user
        );
        moodRepository.save(newEntry);

        return newEntry;
    }

    public Mood getLatestMoodEntry(Authentication auth) {
        ApplicationUser user = userService.getApplicationUser(auth);

        return moodRepository.findFirstByUserOrderByTimestampDesc(user);
    }

    public List<Mood> getAllMoodsThisMonth(Authentication auth) {
        ApplicationUser user = userService.getApplicationUser(auth);

        LocalDateTime date1 = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .withDayOfMonth(1);
        LocalDateTime date2 = LocalDateTime.now()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0)
                .withDayOfMonth(1)
                .plusMonths(1)
                .minusDays(1);

        return moodRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(user, date1, date2);
    }

    public Mood findMoodById(long id) {
        return moodRepository.findById(id).orElseThrow(()-> new NullPointerException("Mood " + id +" not found"));
    }

    public Mood updateMood(long id, HashMap<String, Object> body) {
        if(!(body.containsKey("feeling") || body.containsKey("note") || body.containsKey("timestamp"))) {
            throw new IllegalArgumentException("Please provide a correct attribute to update");
        }

        Mood mood = findMoodById(id);

        if(body.containsKey("feeling")){
            if(!((int)body.get("feeling") >= 0 && (int)body.get("feeling") <= 8)) {
                throw new IllegalArgumentException("Invalid feeling integer value");
            }
            mood.setFeeling((int) body.get("feeling"));
        }
        if(body.containsKey("note")){
            mood.setNote((String) body.get("note"));
        }
        if(body.containsKey("timestamp")){
            mood.setTimestamp((LocalDateTime) body.get("timestamp"));
        }

        moodRepository.save(mood);

        return mood;
    }

    public Mood deleteMood(long id) {
        Mood mood = findMoodById(id);
        moodRepository.delete(mood);
        return mood;
    }
}
