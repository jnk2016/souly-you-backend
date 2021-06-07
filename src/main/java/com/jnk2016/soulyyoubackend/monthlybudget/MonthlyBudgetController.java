package com.jnk2016.soulyyoubackend.monthlybudget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;

@RestController
@RequestMapping("/budget")
public class MonthlyBudgetController {
    @Autowired
    private MonthlyBudgetService monthlyBudgetService;

    @GetMapping
    public ResponseEntity<HashMap<String,Object>> getCurrentBudget(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            MonthlyBudget currentBudget = monthlyBudgetService.getCurrentBudget(auth);
            return ResponseEntity.ok(monthlyBudgetService.budgetToJsonBody(currentBudget));
        } catch(Exception e) {
            response.put("message", e.getMessage());
            if(e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            else {
                return ResponseEntity.badRequest().body(response);
            }
        }
    }

    @PostMapping
    public ResponseEntity<HashMap<String,Object>> createNewBudget(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            MonthlyBudget currentBudget = monthlyBudgetService.createNewBudget(auth);
            if (currentBudget == null) {
                response.put("message", "This month's budget already exists!");
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(monthlyBudgetService.budgetToJsonBody(currentBudget));
            }
        } catch(Exception e) {
            response.put("message", e.getMessage());
            if (e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/date")
    public ResponseEntity<HashMap<String,Object>> getBudgetByDate(Authentication auth, HashMap<String, Object> body) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            MonthlyBudget budget = monthlyBudgetService.getBudgetByDate(auth, (int)body.get("month"), (int)body.get("year"));
            return ResponseEntity.ok(monthlyBudgetService.budgetToJsonBody(budget));
        } catch(Exception e) {
            response.put("message", e.getMessage());
            if (e.getClass() == EntityNotFoundException.class) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            else {
                return ResponseEntity.badRequest().body(response);
            }
        }
    }

    @PutMapping
    public ResponseEntity<HashMap<String,Object>> updateBudgetGoal(@PathVariable long id, HashMap<String, Object> body) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            MonthlyBudget budget = monthlyBudgetService.changeBudgetGoal(id, body);
            response.put("balance", budget.getBalance());
            response.put("budget_remaining", budget.getBudgetRemaining());
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            response.put("message", e.getMessage());
            if(e.getClass() == NullPointerException.class) {
                return ResponseEntity.status(HttpStatus.GONE).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }
    }
}
