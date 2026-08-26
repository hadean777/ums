package com.hadean777.ums.service;

import com.hadean777.ums.entity.Permission;
import com.hadean777.ums.entity.User;
import com.hadean777.ums.model.InternalUserModel;
import com.hadean777.ums.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final com.hadean777.ums.repository.PermissionRepository permissionRepository;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, com.hadean777.ums.repository.PermissionRepository permissionRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
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

    //TODO: make it cacheable
    public InternalUserModel getUserModelByLogin(String login) {
        Optional<User> userOpt = repository.findByLogin(login);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            InternalUserModel result = new InternalUserModel();
            result.setUserId(user.getId());
            result.setAdmin("ADMIN".equals(user.getAuthRole()));
            Set<Permission> dbPermissions = user.getPermissions();
            if (dbPermissions != null && !dbPermissions.isEmpty()) {
                Set<String> permissions = new HashSet<>();
                for (Permission permission : dbPermissions) {
                    permissions.add(permission.getName());
                }
                result.setPermissions(permissions);
            }
            return result;
        }
        return null;
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

        if (user.getId() == null) {
            // New user, set default permission
            if (user.getEnabled() == null) {
                user.setEnabled(true);
            }
            if (user.getPermissions() == null || user.getPermissions().isEmpty()) {
                if (user.getPermissions() == null) {
                    user.setPermissions(new HashSet<>());
                }
                permissionRepository.findById(1L).ifPresent(p -> user.getPermissions().add(p));
            }
        }

        repository.save(user);
    }

}
