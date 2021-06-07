package com.jnk2016.soulyyoubackend.savingsgoal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;

@RestController
@RequestMapping("/savings")
public class SavingsGoalController {
    @Autowired
    SavingsGoalService savingsGoalService;

    @GetMapping
    public ResponseEntity<Object> getAllUserSavingsGoals(Authentication auth) {
        try {
            return ResponseEntity.ok(savingsGoalService.getAllUserSavingsGoals(auth));
        } catch (Exception e){
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            if(e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> createNewSavingsGoal(Authentication auth, HashMap<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(savingsGoalService.toJsonBody(savingsGoalService.createNewSavingsGoal(auth, body)));
        } catch (Exception e){
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            if(e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping
    public ResponseEntity<HashMap<String, Object>> updateSavingsGoal(@PathVariable long id, HashMap<String, Object> body) {
        try {
            return ResponseEntity.ok(savingsGoalService.toJsonBody(savingsGoalService.updateSavingsGoal(id, body)));
        } catch (Exception e){
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            if(e.getClass() == NullPointerException.class) {
                return ResponseEntity.status(HttpStatus.GONE).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping
    public ResponseEntity<HashMap<String, String>> deleteSavingsGoal(@PathVariable long id) {
        HashMap<String, String> response = new HashMap<>();
        try {
            savingsGoalService.deleteSavingsGoal(id);
            response.put("message", "Savings Goal " + id + " successfully deleted!");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            response.put("message", e.getMessage());
            if(e.getClass() == NullPointerException.class) {
                return ResponseEntity.status(HttpStatus.GONE).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }
}
