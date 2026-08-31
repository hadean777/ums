package com.hadean777.ums.controller;

import com.hadean777.ums.entity.User;
import com.hadean777.ums.model.InternalUserModel;
import com.hadean777.ums.service.DeviceService;
import com.hadean777.ums.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    private final UserService userService;
    private final DeviceService deviceService;
    private final com.hadean777.ums.repository.PermissionRepository permissionRepository;

    public WebController(UserService userService, DeviceService deviceService, com.hadean777.ums.repository.PermissionRepository permissionRepository) {
        this.userService = userService;
        this.deviceService = deviceService;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (model.containsAttribute("error")) {
            model.addAttribute("errorMessage", "Wrong credentials");
        }
        return "login";
    }

    @GetMapping("/main")
    public String main(Model model,
                       Authentication authentication,
                       @RequestParam(defaultValue = "0") int page) {
        InternalUserModel userModel = userService.getUserModelByLogin(authentication.getName());
        if (userModel != null) {
            boolean isAdmin = userModel.isAdmin();
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("userModel", userModel);
            model.addAttribute("users", userService.getUsers(PageRequest.of(page, 30)));
            model.addAttribute("devices", deviceService.getDevicesForUser(userModel.getUserId()));
        }
        return "main";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return main(model, authentication, 0);
        }
        try {
            userService.changePassword(authentication.getName(), currentPassword, newPassword);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return main(model, authentication, 0);
        }
        return "redirect:/main";
    }

    @PostMapping("/device/create")
    public String createDevice(Authentication authentication,
                               @RequestParam(required = false) String generationMode,
                               @RequestParam(required = false) String description) throws Exception {
        InternalUserModel userModel = userService.getUserModelByLogin(authentication.getName());
        if (userModel != null) {
            try {
                deviceService.generateNewDevice(userModel.getUserId(), generationMode, description);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "redirect:/main";
    }

    @GetMapping("/device/edit/{id}")
    public String editDeviceForm(@PathVariable Long id, Model model) {
        deviceService.getDeviceById(id).ifPresent(device -> model.addAttribute("device", device));
        return "device_form";
    }

    @PostMapping("/device/save")
    public String saveDevice(@ModelAttribute com.hadean777.ums.entity.Device device) {
        deviceService.updateDevice(device.getId(), device.getDescription(), device.getEnabled());
        return "redirect:/main";
    }

    @PostMapping("/device/delete/{id}")
    public String deleteDevice(@PathVariable Long id, Authentication authentication) throws Exception {
        deviceService.getDeviceById(id).ifPresent(device -> {
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isOwner = userService.getUserByLogin(authentication.getName())
                    .map(user -> user.getId().equals(device.getUserId()))
                    .orElse(false);

            if (isAdmin || isOwner) {
                try {
                    deviceService.deleteDevice(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return "redirect:/main";
    }

    @GetMapping("/user/create")
    public String createUserForm(Model model) {
        User user = new User();
        user.setEnabled(true);
        java.util.Set<com.hadean777.ums.entity.Permission> permissions = new java.util.HashSet<>();
        permissionRepository.findById(1L).ifPresent(permissions::add);
        user.setPermissions(permissions);
        model.addAttribute("user", user);
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "user_form";
    }

    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user) {
        userService.saveUser(user);
        return "redirect:/main";
    }

    @GetMapping("/user/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        userService.getUserById(id).ifPresent(user -> model.addAttribute("user", user));
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "user_form";
    }

    @GetMapping("/device/config/{id}")
    public ResponseEntity<byte[]> getDeviceConfig(@PathVariable Long id, Authentication authentication) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    // Check if the user is an admin or the owner of the device
                    boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    boolean isOwner = userService.getUserByLogin(authentication.getName())
                            .map(user -> user.getId().equals(device.getUserId()))
                            .orElse(false);

                    if (isAdmin || isOwner) {
                        String config = deviceService.generateDeviceConfig(device);
                        byte[] configBytes = config.getBytes();
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wg-config-" + id + ".conf\"")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(configBytes);
                    } else {
                        return ResponseEntity.status(403).<byte[]>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/device/qrcode/{id}")
    public ResponseEntity<byte[]> getDeviceQRCode(@PathVariable Long id, Authentication authentication) {
        return deviceService.getDeviceById(id)
                .map(device -> {
                    // Check if the user is an admin or the owner of the device
                    boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    boolean isOwner = userService.getUserByLogin(authentication.getName())
                            .map(user -> user.getId().equals(device.getUserId()))
                            .orElse(false);

                    if (isAdmin || isOwner) {
                        try {
                            String config = deviceService.generateDeviceConfig(device);
                            byte[] qrCode = deviceService.generateQRCode(config, 300, 300);
                            return ResponseEntity.ok()
                                    .contentType(MediaType.IMAGE_PNG)
                                    .body(qrCode);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return ResponseEntity.status(500).<byte[]>build();
                        }
                    } else {
                        return ResponseEntity.status(403).<byte[]>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }
}
