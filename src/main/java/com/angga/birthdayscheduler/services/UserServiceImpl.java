package com.angga.birthdayscheduler.services;

import com.angga.birthdayscheduler.model.User;
import com.angga.birthdayscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public List<User> findByBirthdayDate(String date) {
        return repository.findByBirthdayDate(date);
    }

}
