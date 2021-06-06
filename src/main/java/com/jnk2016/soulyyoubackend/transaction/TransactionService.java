package com.jnk2016.soulyyoubackend.transaction;

import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudget;
import com.jnk2016.soulyyoubackend.monthlybudget.MonthlyBudgetService;
import com.jnk2016.soulyyoubackend.user.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    MonthlyBudgetService monthlyBudgetService;
    @Autowired
    UserService userService;

    @SneakyThrows(NullPointerException.class)
    public List<HashMap<String, Object>> toJsonBody(List<Transaction> transactions) {
        List<String> filters = Stream.of("user", "budget")
                .collect(Collectors.toList());
        if(transactions == null || transactions.size() == 0){
            throw new NullPointerException("No transactions found");
        }
        List<HashMap<String, Object>> response = new ArrayList<>();
        for(Transaction transaction : transactions) {
            HashMap<String, Object> newJson = userService.toJsonBody(Transaction.class, transaction, filters);
            newJson.put("user_id", transaction.getUser().getUserId());
            newJson.put("budget_id", transaction.getBudget().getBudgetId());
            response.add(newJson);
        }
        return response;
    }

    public List<HashMap<String, Object>> getAllExpensesInCurrentBudget(Authentication auth) {
        MonthlyBudget currentBudget = monthlyBudgetService.getCurrentBudget(auth);
        return toJsonBody(transactionRepository
                .findByBudgetAndCompletedAndTypeNotOrderByTimestampDesc(    // queries only for completed payments and expenses (orders by most recent)
                        currentBudget,true, "income"));
    }

    public HashMap<String, Object> getAllIncomeInCurrentBudget(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        MonthlyBudget currentBudget = monthlyBudgetService.getCurrentBudget(auth);

        List<HashMap<String, Object>> received =  toJsonBody(transactionRepository
                .findByBudgetAndCompletedAndTypeOrderByTimestampDesc(       // queries only for completed incomes (orders by most recent)
                        currentBudget,true, "income"));

        List<HashMap<String, Object>> pending =  toJsonBody(transactionRepository
                .findByBudgetAndCompletedAndTypeOrderByTimestampDesc(       // queries only for completed incomes (orders by most recent)
                        currentBudget,false, "income"));

        response.put("received_incomes", received);
        response.put("pending_incomes", pending);

        return response;
    }

    public HashMap<String, Object> getUserPayments(Authentication auth) {
        HashMap<String, Object> response = new HashMap<>();
        MonthlyBudget currentBudget = monthlyBudgetService.getCurrentBudget(auth);

        List<HashMap<String, Object>> completed = new ArrayList<>();
        HashMap<String, Object> completedResponse = new HashMap<>();
        try {
            completed = toJsonBody(transactionRepository
                    .findByBudgetAndTypeAndCompletedOrderByTimestampAsc(
                            currentBudget, "payment", true));
            completedResponse.put("total", completed.size());
        } catch (NullPointerException e) {
            completedResponse.put("total", 0);
        }
        completedResponse.put("items", completed);

        List<HashMap<String, Object>> pending = new ArrayList<>();
        HashMap<String, Object> pendingResponse = new HashMap<>();
        try {
            pending = toJsonBody(transactionRepository
                .findByBudgetAndTypeAndCompletedAndTimestampAfterOrderByTimestampAsc(
                        currentBudget, "payment", false, LocalDateTime.now()));
            pendingResponse.put("total", (pending.size()));
        } catch (NullPointerException e) {
            pendingResponse.put("total", 0);
        }
        pendingResponse.put("items", pending);

        List<HashMap<String, Object>> overdue = new ArrayList<>();
        HashMap<String, Object> overdueResponse = new HashMap<>();
        try {
            overdue = toJsonBody(transactionRepository
                    .findByUserAndTypeAndCompletedAndTimestampBeforeOrderByTimestampAsc(
                            userService.getApplicationUser(auth), "payment", false, LocalDateTime.now()));
            overdueResponse.put("total", (overdue.size()));
        } catch (NullPointerException e) {
            overdueResponse.put("total", 0);
        }
        overdueResponse.put("items", overdue);

        response.put("overdue_payments", overdueResponse);
        response.put("pending_payments", pendingResponse);
        response.put("completed_payments", completedResponse);
        response.put("total_payments", (int)overdueResponse.get("total") + (int)pendingResponse.get("total") + (int)completedResponse.get("total"));
        return response;
    }

    public double newTransaction(Authentication auth, HashMap<String, Object> body) {
        Transaction newEntry = new Transaction();
        newEntry.setUser(userService.getApplicationUser(auth));
        newEntry.setName((String)body.get("name"));
        newEntry.setAmount((double)body.get("amount"));
        newEntry.setBudget(monthlyBudgetService.getCurrentBudget(auth));
        newEntry.setType((String)body.get("type"));
        newEntry.setTimestamp((LocalDateTime)body.get("timestamp"));
        newEntry.setCompleted(newEntry.getType().equals("expense") || (boolean) body.get("completed"));
        transactionRepository.save(newEntry);

        double updatedTypeTotal = 0.00;
        if(newEntry.getType().equals("income") && newEntry.isCompleted()){
            updatedTypeTotal = monthlyBudgetService.updateIncome(newEntry.getBudget(), newEntry.getAmount());
        }
        else if(newEntry.getType().equals("expense") || (newEntry.getType().equals("payment") && newEntry.isCompleted())) {
            updatedTypeTotal = monthlyBudgetService.updateExpenses(newEntry.getBudget(), newEntry.getAmount());
        }
        else if(newEntry.getType().equals("income") && !newEntry.isCompleted()){
            updatedTypeTotal = newEntry.getBudget().getIncome();
        }
        else if(newEntry.getType().equals("payment") && !newEntry.isCompleted()) {
            updatedTypeTotal = newEntry.getBudget().getExpenses();
        }
        return updatedTypeTotal;
    }

    public double removeTransaction(long id) {
        Transaction entry = findTransactionById(id);
        double updatedIncomeOrExpenseTotal = -1.00;
        if (entry.getType().equals("income") && entry.isCompleted()) {
            updatedIncomeOrExpenseTotal = monthlyBudgetService.updateIncome(entry.getBudget(), entry.getAmount() * (-1));
        }
        else if(entry.getType().equals("expense") || (entry.getType().equals("payment") && entry.isCompleted())) {
            updatedIncomeOrExpenseTotal = monthlyBudgetService.updateExpenses(entry.getBudget(), entry.getAmount() * (-1));
        }
        else if(entry.getType().equals("income") && !entry.isCompleted()){
            updatedIncomeOrExpenseTotal = entry.getBudget().getIncome();
        }
        else if(entry.getType().equals("payment") && !entry.isCompleted()) {
            updatedIncomeOrExpenseTotal = entry.getBudget().getExpenses();
        }
        transactionRepository.delete(entry);
        return updatedIncomeOrExpenseTotal;
    }

    public double updateTransaction(long id, HashMap<String, Object> body) {
        if(!(body.containsKey("name") && body.containsKey("amount") && body.containsKey("completed") && body.containsKey("timestamp"))) {
            throw new NullPointerException("Incorrect JSON body");
        }

        Transaction entry = findTransactionById(id);
        double oldAmount = entry.getAmount();
        boolean oldCompletion = entry.isCompleted();

        entry.setName((String) body.get("name"));
        entry.setAmount((double) body.get("amount"));
        entry.setCompleted((boolean) body.get("completed"));
        entry.setTimestamp((LocalDateTime) body.get("timestamp"));

        double updatedIncomeOrExpenseTotal = -1.00;
        if (entry.getType().equals("income") && entry.isCompleted()) {
            if(!oldCompletion) {
                oldAmount = 0.00;
            }
            updatedIncomeOrExpenseTotal = monthlyBudgetService.updateIncome(entry.getBudget(), entry.getAmount() + (oldAmount * (-1)));
        }
        else if(entry.getType().equals("expense") || (entry.getType().equals("payment") && entry.isCompleted())) {
            if(!oldCompletion) {
                oldAmount = 0.00;
            }
            updatedIncomeOrExpenseTotal = monthlyBudgetService.updateExpenses(entry.getBudget(), entry.getAmount() + (oldAmount * (-1)));
        }
        else if(entry.getType().equals("income") && !entry.isCompleted()){
            oldAmount *= -1;
                updatedIncomeOrExpenseTotal = oldCompletion ?
                    monthlyBudgetService.updateIncome(entry.getBudget(), oldAmount * (-1)) :
                        entry.getBudget().getIncome();
        }
        else if(entry.getType().equals("payment") && !entry.isCompleted()) {
            updatedIncomeOrExpenseTotal = oldCompletion ?
                    monthlyBudgetService.updateExpenses(entry.getBudget(), oldAmount * (-1)) :
                    entry.getBudget().getExpenses();
        }
        transactionRepository.save(entry);
        return updatedIncomeOrExpenseTotal;
    }

    @SneakyThrows(NullPointerException.class)
    public Transaction findTransactionById(long id) {
        return transactionRepository.findById(id).orElseThrow(()-> new NullPointerException("Transaction not found or has been removed"));
    }

    @SneakyThrows(NullPointerException.class)
    public HashMap<String, Object> completeTransaction(String pathParam, String type) {
        if(pathParam.equals("")) {
            throw new NullPointerException("No IDs given");
        }

        HashMap<String, Object> response = new HashMap<>();
        List<Transaction> updatedTransactions = new ArrayList<>();
        List<Long> invalidIds = new ArrayList<>();
        double updateAmount = 0.00;
        long mostRecentId = 0L;
        Transaction mostRecentTransaction = new Transaction();

        String[] stringIdsArr = pathParam.split(",");
        for(String stringId : stringIdsArr) {
            long id = Long.parseLong(stringId);
            try {
                Transaction transaction = findTransactionById(id);
                if(transaction.getType().equals(type) && !transaction.isCompleted()) {
                    transaction.setCompleted(true);
                    updateAmount += transaction.getAmount();

                    if(mostRecentId < transaction.getTransactionId()) {
                        mostRecentId = transaction.getTransactionId();
                        mostRecentTransaction = transaction;
                    }

                    transactionRepository.save(transaction);
                    updatedTransactions.add(transaction);
                }
                else {
                    invalidIds.add(id);
                }
            } catch (NullPointerException e) {
                invalidIds.add(id);
            }
        }

        response.put("new_type_total", type.equals("payment") ?
                monthlyBudgetService.updateExpenses(mostRecentTransaction.getBudget(), updateAmount) :
                monthlyBudgetService.updateIncome(mostRecentTransaction.getBudget(), updateAmount) );
        response.put("updated_transactions", toJsonBody(updatedTransactions));
        response.put("total_transactions_updated", updatedTransactions.size());
        response.put("invalid_ids", invalidIds);
        response.put("total_invalid_ids", invalidIds.size());
        return response;
    }

    public HashMap<String, Object> completePayments(String pathParam) {
        HashMap<String, Object> response = new HashMap<>();
        HashMap<String, Object> completedTransaction = completeTransaction(pathParam, "payment");

        response.put("new_expenses_total", completedTransaction.get("new_type_total"));
        response.put("updated_payments", completedTransaction.get("updated_transactions"));
        response.put("total_payments_updated", completedTransaction.get("total_transactions_updated"));
        response.put("invalid_ids", completedTransaction.get("new_type_total"));
        response.put("total_invalid_ids", completedTransaction.get("new_type_total"));
        return response;
    }

    public HashMap<String, Object> receiveIncomes(String pathParam) {
        HashMap<String, Object> response = new HashMap<>();
        HashMap<String, Object> completedTransaction = completeTransaction(pathParam, "income");

        response.put("new_income_total", completedTransaction.get("new_type_total"));
        response.put("updated_incomes", completedTransaction.get("updated_transactions"));
        response.put("total_incomes_updated", completedTransaction.get("total_transactions_updated"));
        response.put("invalid_ids", completedTransaction.get("new_type_total"));
        response.put("total_invalid_ids", completedTransaction.get("new_type_total"));
        return response;
    }
}
