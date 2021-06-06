package com.jnk2016.soulyyoubackend.savingsgoal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
