package com.hadean777.ums.service;

import com.hadean777.ums.entity.User;
import com.hadean777.ums.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLogin(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getLogin())
                        .password(user.getPasswd())
                        .roles(user.getAuthRole())
                        .disabled(!user.getEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public Page<User> getUsers(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> getUserByLogin(String login) {
        return repository.findByLogin(login);
    }

    public void saveUser(User user) {
        if (user.getPasswd() != null && !user.getPasswd().isEmpty()) {
            if (!user.getPasswd().startsWith("$2a$")) {
                user.setPasswd(passwordEncoder.encode(user.getPasswd()));
            }
        } else if (user.getId() != null) {
            // If editing and password is empty, keep the old one
            repository.findById(user.getId()).ifPresent(existingUser -> user.setPasswd(existingUser.getPasswd()));
        }
        repository.save(user);
    }

}
