package com.hadean777.ums;

import com.hadean777.ums.repository.InviteLinkRepository;
import com.hadean777.ums.repository.PermissionRepository;
import com.hadean777.ums.repository.UserRepository;
import com.hadean777.ums.service.DeviceService;
import com.hadean777.ums.service.UserService;
import com.hadean777.ums.service.WireGuardService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class UmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }

    @Bean
    public UserService userService(UserRepository repository,
                                   PasswordEncoder passwordEncoder,
                                   PermissionRepository permissionRepository,
                                   InviteLinkRepository inviteLinkRepository,
                                   @Lazy SessionRegistry sessionRegistry,
                                   @Lazy DeviceService deviceService,
                                   @Lazy WireGuardService wireGuardService) {
        return new UserService(repository, passwordEncoder, permissionRepository, inviteLinkRepository,
                sessionRegistry, deviceService, wireGuardService);
    }

}
