package com.hadean777.ums.service;

import com.hadean777.ums.entity.User;
import com.hadean777.ums.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }


}
