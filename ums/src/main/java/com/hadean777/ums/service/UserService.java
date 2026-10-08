package com.hadean777.ums.service;

import com.hadean777.ums.Constants;
import com.hadean777.ums.entity.Device;
import com.hadean777.ums.entity.InviteLink;
import com.hadean777.ums.entity.Permission;
import com.hadean777.ums.entity.User;
import com.hadean777.ums.model.InternalUserModel;
import com.hadean777.ums.repository.InviteLinkRepository;
import com.hadean777.ums.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.hadean777.ums.Constants.CLIENT_ALLOWED_IPS;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final com.hadean777.ums.repository.PermissionRepository permissionRepository;
    private final InviteLinkRepository inviteLinkRepository;
    private final SessionRegistry sessionRegistry;
    private final DeviceService deviceService;
    private final WireGuardService wireGuardService;

    public UserService(UserRepository repository,
                       PasswordEncoder passwordEncoder,
                       com.hadean777.ums.repository.PermissionRepository permissionRepository,
                       InviteLinkRepository inviteLinkRepository,
                       SessionRegistry sessionRegistry,
                       DeviceService deviceService,
                       WireGuardService wireGuardService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.inviteLinkRepository = inviteLinkRepository;
        this.sessionRegistry = sessionRegistry;
        this.deviceService = deviceService;
        this.wireGuardService = wireGuardService;
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
        Boolean oldEnabled = null;
        boolean useOldPassword = true;
        if (user.getPasswd() != null && !user.getPasswd().isEmpty()) {
            if (!user.getPasswd().startsWith("$2a$")) {
                user.setPasswd(passwordEncoder.encode(user.getPasswd()));
                useOldPassword = false;
            }
        }
        if (user.getId() != null) {
            // If editing and password is empty, keep the old one
            Optional<User> existingUserOpt = repository.findById(user.getId());
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                if (useOldPassword) {
                    user.setPasswd(existingUser.getPasswd());
                }
                oldEnabled = existingUser.getEnabled();
                if (user.getCreatedBy() == null) {
                    user.setCreatedBy(existingUser.getCreatedBy());
                }
            }
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

        // Check if enabled status changed
        if (user.getId() != null && oldEnabled != null && !oldEnabled.equals(user.getEnabled())) {
            if (Boolean.FALSE.equals(user.getEnabled())) {
                // Remove active sessions
                List<Object> principals = sessionRegistry.getAllPrincipals();
                for (Object principal : principals) {
                    if (principal instanceof UserDetails) {
                        UserDetails userDetails = (UserDetails) principal;
                        if (userDetails.getUsername().equals(user.getLogin())) {
                            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                            for (SessionInformation session : sessions) {
                                session.expireNow();
                            }
                        }
                    }
                }
                // Disable devices on WG side
                List<Device> devices = deviceService.getDevicesForUser(user.getId());
                for (Device device : devices) {
                    try {
                        wireGuardService.removePeer(device.getPublicKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (Boolean.TRUE.equals(user.getEnabled())) {
                // Restore devices on WG side where device.enabled = true
                List<Device> devices = deviceService.getDevicesForUser(user.getId());
                for (Device device : devices) {
                    if (Boolean.TRUE.equals(device.getEnabled())) {
                        try {
                            wireGuardService.addPeer(device.getPublicKey(), CLIENT_ALLOWED_IPS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void changePassword(String login, String currentPassword, String newPassword) {
        repository.findByLogin(login).ifPresent(user -> {
            if (passwordEncoder.matches(currentPassword, user.getPasswd())) {
                user.setPasswd(passwordEncoder.encode(newPassword));
                repository.save(user);
            } else {
                throw new RuntimeException("Current password does not match");
            }
        });
    }

    public List<InviteLink> getAllInviteLinks() {
        return inviteLinkRepository.findAll();
    }

    public String generateInviteLink(Long expirationMillis, Long createdBy) {
        if (expirationMillis == null) {
            expirationMillis = Constants.DEFAULT_EXPIRATION_TIME;
        }
        if (expirationMillis > Constants.MAX_EXPIRATION_TIME) {
            expirationMillis = Constants.MAX_EXPIRATION_TIME;
        }

        InviteLink inviteLink = new InviteLink();
        inviteLink.setToken(UUID.randomUUID().toString());
        inviteLink.setExpirationTime(LocalDateTime.now().plusNanos(expirationMillis * 1_000_000).toEpochSecond(ZoneOffset.UTC));
        inviteLink.setUsed(false);
        inviteLink.setCreatedBy(createdBy);
        inviteLinkRepository.save(inviteLink);

        return inviteLink.getToken();
    }

    public Optional<InviteLink> getInviteLink(String token) {
        final long nowSeconds = new Date().getTime() / 1000;
        return inviteLinkRepository.findByToken(token)
                .filter(link -> !link.isUsed() && link.getExpirationTime() > nowSeconds);
    }

    @Transactional
    public void registerUser(String token, String login, String password) {
        InviteLink inviteLink = getInviteLink(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired invite link"));

        if (repository.findByLogin(login).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setLogin(login);
        user.setPasswd(passwordEncoder.encode(password));
        user.setAuthRole("USER");
        user.setEnabled(true);
        user.setCreatedBy(inviteLink.getCreatedBy());
        user.setPermissions(new HashSet<>());
        permissionRepository.findById(1L).ifPresent(p -> user.getPermissions().add(p));

        repository.save(user);

        inviteLink.setUsed(true);
        inviteLinkRepository.save(inviteLink);
    }

}
