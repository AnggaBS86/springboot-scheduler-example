package com.angga.birthdayscheduler.services;

import com.angga.birthdayscheduler.model.User;
import com.angga.birthdayscheduler.model.UserBirthdayLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserBirthdayLogService {
    List<UserBirthdayLog> findByUserId(int userId);

    boolean store(UserBirthdayLog userBirthdayLog);
}
