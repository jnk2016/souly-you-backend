package com.jnk2016.soulyyoubackend.mood;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;

@RestController
@RequestMapping("/mood")
public class MoodController {
    @Autowired
    MoodService moodService;

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> createNewMood(Authentication auth, HashMap<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(moodService.toJsonBody(moodService.createNewMood(auth, body)));
        } catch (Exception e) {
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            if (e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<HashMap<String, Object>> getLatestMoodEntry(Authentication auth) {
        try {
            return ResponseEntity.ok(moodService.toJsonBody(moodService.getLatestMoodEntry(auth)));
        } catch (Exception e) {
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            if (e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }
    }

    @PutMapping
    public ResponseEntity<HashMap<String, Object>> updateMood(@PathVariable long id, HashMap<String, Object> body) {
        try {
            return ResponseEntity.ok(moodService.toJsonBody(moodService.updateMood(id, body)));
        } catch (Exception e) {
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            if (e.getClass() == NullPointerException.class) {
                return ResponseEntity.status(HttpStatus.GONE).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping
    public ResponseEntity<HashMap<String, Object>> deleteMood(@PathVariable long id) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            moodService.deleteMood(id);
            response.put("message", "Mood successfully deleted!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            if (e.getClass() == NullPointerException.class) {
                return ResponseEntity.status(HttpStatus.GONE).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }
}
