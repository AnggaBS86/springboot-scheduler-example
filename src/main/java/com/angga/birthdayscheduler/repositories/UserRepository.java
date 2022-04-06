package com.angga.birthdayscheduler.repositories;


import com.angga.birthdayscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    public List<User> findByBirthdayDate(String date);
}
