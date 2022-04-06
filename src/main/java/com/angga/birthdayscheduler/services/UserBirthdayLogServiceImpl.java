package com.angga.birthdayscheduler.services;

import com.angga.birthdayscheduler.model.User;
import com.angga.birthdayscheduler.model.UserBirthdayLog;
import com.angga.birthdayscheduler.repositories.UserBirthdayLogRepository;
import com.angga.birthdayscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Component
public class UserBirthdayLogServiceImpl implements UserBirthdayLogService {

    @Autowired
    private UserBirthdayLogRepository repository;

    @Override
    public List<UserBirthdayLog> findByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public boolean store(UserBirthdayLog userBirthdayLog) {
        try {
            repository.save(userBirthdayLog);
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
