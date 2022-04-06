package com.angga.birthdayscheduler.services;

import com.angga.birthdayscheduler.model.User;
import com.angga.birthdayscheduler.model.UserBirthdayLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<User> findByBirthdayDate(String date);
}
