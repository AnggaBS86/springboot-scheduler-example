package com.angga.birthdayscheduler.repositories;

import com.angga.birthdayscheduler.model.UserBirthdayLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBirthdayLogRepository extends JpaRepository<UserBirthdayLog, Integer> {
    List<UserBirthdayLog> findByUserId(int userId);
}