package com.jnk2016.soulyyoubackend.mood;

import com.jnk2016.soulyyoubackend.user.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {
    Mood findFirstByUserOrderByTimestampDesc(ApplicationUser user);
    List<Mood> findByUserAndTimestampBetweenOrderByTimestampDesc(ApplicationUser user, LocalDateTime date1, LocalDateTime date2);
}
