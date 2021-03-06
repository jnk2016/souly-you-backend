package com.jnk2016.soulyyoubackend.savingsgoal;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import com.jnk2016.soulyyoubackend.user.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SavingsGoalService {
    @Autowired
    SavingsGoalRepository savingsGoalRepository;
    @Autowired
    UserService userService;

    public HashMap<String, Object> toJsonBody(SavingsGoal savingsGoal) {
        List<String> filters = Stream.of("user").collect(Collectors.toList());
        HashMap<String, Object> response = userService.toJsonBody(SavingsGoal.class, savingsGoal, filters);
        response.put("user_id", savingsGoal.getUser().getUserId());

        return response;
    }

    public List<HashMap<String, Object>> getAllUserSavingsGoals(Authentication auth) {
        List<HashMap<String,Object>> response = new ArrayList<>();

        ApplicationUser user = userService.getApplicationUser(auth);    // throws EntityNotFoundException

        List<SavingsGoal> savingsGoals = savingsGoalRepository.findByUser(user);
        if(savingsGoals == null || savingsGoals.size() == 0) { return new ArrayList<>(); }

        for(SavingsGoal savingsGoal : savingsGoals) {
            response.add(toJsonBody(savingsGoal));
        }
        return response;
    }

    @SneakyThrows(NullPointerException.class)
    public SavingsGoal findSavingsGoalById(long id) {
        return savingsGoalRepository.findById(id).orElseThrow(()-> new NullPointerException("Savings Goal " + id +" not found"));
    }

    public SavingsGoal createNewSavingsGoal(Authentication auth, HashMap<String, Object> body) {
        ApplicationUser user = userService.getApplicationUser(auth);
        SavingsGoal newEntry = new SavingsGoal();

        if(!(body.containsKey("name") && body.containsKey("goal_amount") && body.containsKey("saved_amount") && body.size() == 3)) {
            throw new IllegalArgumentException("Incorrect formatting of request body");
        }
        else if(!((double)body.get("goal_amount") > 0.00 && (double)body.get("saved_amount") >= 0.00)) {
            throw new IllegalArgumentException("Invalid value for saved and/or goal amount");
        }

        newEntry.setUser(user);
        newEntry.setName((String)body.get("name"));
        newEntry.setGoalAmount((double)body.get("goal_amount"));
        newEntry.setSavedAmount((double)body.get("saved_amount"));
        newEntry.setComplete(newEntry.getSavedAmount() >= newEntry.getGoalAmount());
        savingsGoalRepository.save(newEntry);

        return newEntry;
    }

    public SavingsGoal updateSavingsGoal(long id, HashMap<String, Object> body) {
        SavingsGoal savingsGoal = findSavingsGoalById(id);

        if(!(body.containsKey("name") || body.containsKey("goal_amount") || body.containsKey("saved_amount"))) {
            throw new IllegalArgumentException("Please provide at least one attribute to update");
        }

        if(body.containsKey("name")){
            savingsGoal.setName((String) body.get("name"));
        }
        if(body.containsKey("goal_amount")){
            if((double) body.get("goal_amount") <= 0.00) {
                throw new IllegalArgumentException("Invalid value for saved and/or goal amount");
            }
            savingsGoal.setGoalAmount((double) body.get("goal_amount"));
        }
        if(body.containsKey("saved_amount")){
            if((double) body.get("saved_amount") < 0.00) {
                throw new IllegalArgumentException("Invalid value for saved and/or goal amount");
            }
            savingsGoal.setSavedAmount((double) body.get("saved_amount"));
        }
        savingsGoal.setComplete(savingsGoal.getSavedAmount() >= savingsGoal.getGoalAmount());

        savingsGoalRepository.save(savingsGoal);

        return savingsGoal;
    }

    public SavingsGoal deleteSavingsGoal(long id) {
        SavingsGoal savingsGoal = findSavingsGoalById(id);
        savingsGoalRepository.delete(savingsGoal);
        return savingsGoal;
    }
}
