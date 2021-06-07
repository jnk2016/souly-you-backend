package com.jnk2016.soulyyoubackend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /** Register a new account */
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody HashMap<String, String> body) {
        if(body.keySet().size() == 4 && !body.containsValue(null) && !(body.containsKey("username") && body.containsKey("password") && body.containsKey("firstname") && body.containsKey("lastname"))){
            return ResponseEntity.badRequest().body("Request body is incorrect!");
        }

        if(userService.newUser(body)) {
            return ResponseEntity.ok("Your account has been registered!");
        }
        else {
            return ResponseEntity.badRequest().body("This username already exists. Please choose a different username.");
        }
    }

    /** Get the date the user joined */
    @GetMapping("/joined")
    public ResponseEntity<HashMap<String,String>> dateJoined(Authentication auth) {
        HashMap<String, String> response = new HashMap<>();
        try {
            LocalDate date = userService.getDateJoined(auth);
            response.put("Date Joined", date.toString());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            response.put("Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/id")
    public ResponseEntity<HashMap<String,Object>> getUserById(@PathVariable long id) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            ApplicationUser user = userService.getUserById(id);

            response.put("username", user.getUsername());
            response.put("firstname", user.getFirstname());
            response.put("lastname", user.getLastname());
            response.put("date_joined", user.getDateJoined());

            return ResponseEntity.ok(response);
        } catch(NullPointerException e) {
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}