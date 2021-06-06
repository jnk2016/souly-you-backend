package com.jnk2016.soulyyoubackend.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PutMapping("/payments")
    public ResponseEntity<HashMap<String, Object>> completePayments(@RequestParam String ids) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            return ResponseEntity.ok(transactionService.completePayments(ids));
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/incomes")
    public ResponseEntity<HashMap<String, Object>> receiveIncomes(@RequestParam String ids) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            return ResponseEntity.ok(transactionService.receiveIncomes(ids));
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/expenses")
    public ResponseEntity<HashMap<String, Object>> getAllExpensesInCurrentBudget(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            response.put("expenses", transactionService.getAllExpensesInCurrentBudget(auth));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/incomes")
    public ResponseEntity<HashMap<String, Object>> getAllIncomeInCurrentBudget(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            return ResponseEntity.ok(transactionService.getAllIncomeInCurrentBudget(auth));
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<HashMap<String, Object>> getUserPayments(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            return ResponseEntity.ok(transactionService.getUserPayments(auth));
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> newTransaction(Authentication auth, HashMap<String, Object> body) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            response.put("updated_" + body.get("type")+"_total", transactionService.newTransaction(auth, body));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> removeTransaction(@PathVariable long id) {
        try {
            transactionService.removeTransaction(id);
            return ResponseEntity.ok("Transaction removed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<HashMap<String, Object>> updateTransaction(@PathVariable long id, HashMap<String, Object> body) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            response.put("updated_" + body.get("type")+"_total", transactionService.updateTransaction(id, body));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
