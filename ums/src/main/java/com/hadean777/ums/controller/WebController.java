package com.hadean777.ums.controller;

import com.hadean777.ums.entity.User;
import com.hadean777.ums.service.DeviceService;
import com.hadean777.ums.service.UserService;
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

    public WebController(UserService userService, DeviceService deviceService) {
        this.userService = userService;
        this.deviceService = deviceService;
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
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            model.addAttribute("users", userService.getUsers(PageRequest.of(page, 30)));
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("isAdmin", false);
            userService.getUserByLogin(authentication.getName()).ifPresent(user -> {
                model.addAttribute("devices", deviceService.getDevicesForUser(user.getId()));
            });
        }
        return "main";
    }

    @PostMapping("/device/create")
    public String createDevice(Authentication authentication) throws Exception {
        userService.getUserByLogin(authentication.getName()).ifPresent(user -> {
            try {
                deviceService.generateNewDevice(user.getId());
            } catch (Exception e) {
                // Ignore for now
            }
        });
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

    @GetMapping("/user/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
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
        return "user_form";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }
}
