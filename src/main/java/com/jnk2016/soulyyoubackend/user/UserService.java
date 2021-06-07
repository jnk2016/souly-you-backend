package com.jnk2016.soulyyoubackend.user;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @SneakyThrows(EntityNotFoundException.class)
    public ApplicationUser getApplicationUser(Authentication auth) {
        return applicationUserRepository.findByUsername((auth.getName()))
                .orElseThrow(()-> new EntityNotFoundException("Invalid credentials provided"));  // Obtains the current user
    }

    @SneakyThrows({IllegalAccessException.class, ClassNotFoundException.class})
    public HashMap<String, Object> toJsonBody(Class someClass, Object object, List<String> filters) {
        HashMap<String, Object> response = new HashMap<>();
        Class budgetClass = Class.forName(someClass.getPackageName() + "." + someClass.getSimpleName());
//        Field[] allFields = Arrays.stream(budgetClass.getDeclaredFields())
//                .filter(f -> !filters.contains(f.getName()))
//                .collect(Collectors.toList()).toArray(Field[]::new);
        Field[] allFields = budgetClass.getDeclaredFields();
        for(Field field : allFields) {
            if(filters == null || !filters.contains(field.getName())) {
                field.setAccessible(true);
                Object value = field.get((budgetClass.cast(object)));
                response.put(toUnderscoreName(field.getName()), value);
            }
        }
        return response;
    }

    public String toUnderscoreName(String str) {
        String result = str.replaceAll("(.)([A-Z])", "$1_$2");
        return result.toLowerCase();
    }

    public boolean newUser(HashMap<String, String> body) {
        ApplicationUser user = applicationUserRepository.findByUsername(body.get("username"))
                .orElse(null);
        if(user == null) {
            applicationUserRepository.save(new ApplicationUser(body.get("username"), bCryptPasswordEncoder.encode(body.get("password")),
                                            body.get("firstname"), body.get("lastname"), LocalDate.now()));
            return true;
        }
        else { return false; }
    }

    public LocalDate getDateJoined(Authentication auth){
        ApplicationUser user = getApplicationUser(auth);
        return user.getDateJoined();
    }

    @SneakyThrows(NullPointerException.class)
    public ApplicationUser getUserById(long id) {
        return applicationUserRepository.findById(id).orElseThrow(()-> new NullPointerException("No user with id " + id + " found"));
    }
}
