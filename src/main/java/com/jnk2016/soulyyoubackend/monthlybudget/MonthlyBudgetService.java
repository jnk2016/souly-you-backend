package com.jnk2016.soulyyoubackend.monthlybudget;

import com.jnk2016.soulyyoubackend.transaction.Transaction;
import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import com.jnk2016.soulyyoubackend.user.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MonthlyBudgetService {
    @Autowired
    MonthlyBudgetRepository monthlyBudgetRepository;
    @Autowired
    UserService userService;

    public HashMap<String, Object> budgetToJsonBody(MonthlyBudget monthlyBudget) {
        List<String> filters = Stream.of("user", "transactions")
                .collect(Collectors.toList());

        HashMap<String, Object> response = userService.toJsonBody(MonthlyBudget.class, monthlyBudget, filters);

        response.put("user_id", monthlyBudget.getUser().getUserId());
        response.put("transactions", transactionToJsonBody(monthlyBudget));
        return response;
    }

    public List<HashMap<String, Object>> transactionToJsonBody(MonthlyBudget monthlyBudget) {
        List<String> filters = Stream.of("user", "budget")
                .collect(Collectors.toList());
        if(monthlyBudget.getTransactions() == null || monthlyBudget.getTransactions().size() == 0){
            return new ArrayList<>();
        }
        List<HashMap<String, Object>> response = new ArrayList<>();
        for(Transaction transaction : monthlyBudget.getTransactions()) {
            HashMap<String, Object> transactionJson = userService.toJsonBody(Transaction.class, transaction, filters);
            response.add(transactionJson);
        }
        return response;
    }

    public MonthlyBudget initializeFirstMonthlyBudget (ApplicationUser user) {
        MonthlyBudget firstEntry = new MonthlyBudget();
        firstEntry.setMonth(LocalDate.now().getMonthValue());
        firstEntry.setYear(LocalDate.now().getYear());
        firstEntry.setUser(user);
        monthlyBudgetRepository.save(firstEntry);
        return firstEntry;
    }

    public MonthlyBudget addNextBudget(ApplicationUser user) {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        MonthlyBudget mostRecent = monthlyBudgetRepository.findFirstByUserOrderByBudgetIdDesc(user);
        MonthlyBudget newEntry = new MonthlyBudget();
        newEntry.setUser(user);
        newEntry.setBudgetGoal(mostRecent.getBudgetGoal());
        newEntry.setMonth(currentMonth);
        newEntry.setYear(currentYear);
        monthlyBudgetRepository.save(newEntry);
        return newEntry;
    }

    public MonthlyBudget getCurrentBudget(Authentication auth) {
        ApplicationUser user = userService.getApplicationUser(auth);

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        MonthlyBudget current = monthlyBudgetRepository.findByUserAndMonthAndYear(user, currentMonth, currentYear);
        if(current == null) {
            throw new NullPointerException("Budget not found");
        }
        return current;
    }

    public MonthlyBudget createNewBudget(Authentication auth) {
        ApplicationUser user = userService.getApplicationUser(auth);
        
        if(user.getBudgets() == null || user.getBudgets().size() == 0) {
            return initializeFirstMonthlyBudget(user);
        }
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        MonthlyBudget current = monthlyBudgetRepository.findByUserAndMonthAndYear(user, currentMonth, currentYear);
        if(current == null){
            current = addNextBudget(user);
            return current;
        }
        return null;
    }

    public MonthlyBudget changeBudgetGoal (long id, HashMap<String, Object> body) {
        if(body.size() != 1 || !body.containsKey("budget_goal") || body.get("budget_goal") == null || body.get("budget_goal").getClass() != Double.class) {
            throw new IllegalArgumentException("Incorrect JSON body");
        }
        double budgetGoal = (double)body.get("budget_goal");
        if(budgetGoal < 0.00) {
            throw new IllegalArgumentException(("Invalid value for budget_goal"));
        }
        try {
            MonthlyBudget entry = getBudgetById(id);
            entry.setBudgetGoal(budgetGoal);
            entry.setBudgetRemaining(updateBudgetRemaining(entry));
            monthlyBudgetRepository.save(entry);
            return entry;
        } catch (NullPointerException e) {
            throw new NullPointerException("Budget not found");
        }
    }

    public double updateBalance(MonthlyBudget budget) {
        return budget.getIncome()-budget.getExpenses();
    }

    public double updateBudgetRemaining(MonthlyBudget budget) {
        return budget.getBudgetGoal()-budget.getExpenses();
    }

    public double updateIncome(MonthlyBudget budget, double incomeAmount) {
        budget.setIncome(budget.getIncome()+incomeAmount);
        budget.setBalance(updateBalance(budget));
        monthlyBudgetRepository.save(budget);
        return budget.getIncome();
    }

    public double updateExpenses(MonthlyBudget budget, double expenseAmount) {
        budget.setExpenses(budget.getExpenses()+expenseAmount);
        budget.setBalance(updateBalance(budget));
        budget.setBudgetRemaining(updateBudgetRemaining(budget));
        monthlyBudgetRepository.save(budget);
        return budget.getExpenses();
    }

    @SneakyThrows(NullPointerException.class)
    public MonthlyBudget getBudgetById(long id) {
        return monthlyBudgetRepository.findById(id).orElseThrow(()-> new NullPointerException("Budget not found"));
    }

    public MonthlyBudget getBudgetByDate(Authentication auth, int month, int year) {
        ApplicationUser user = userService.getApplicationUser(auth);

        MonthlyBudget budget = monthlyBudgetRepository.findByUserAndMonthAndYear(user, month, year);
        if(budget == null) {
            throw new NullPointerException("Budget not found");
        }
        return budget;
    }
}
