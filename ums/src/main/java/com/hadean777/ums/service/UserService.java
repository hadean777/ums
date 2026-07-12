package com.hadean777.ums.service;

import com.hadean777.ums.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLogin(username)
                .map(user -> User.builder()
                        .username(user.getLogin())
                        .password(user.getPasswd())
                        .roles(user.getAuthRole())
                        .disabled(!user.getEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

}
