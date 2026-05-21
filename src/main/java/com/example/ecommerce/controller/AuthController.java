package com.example.ecommerce.controller;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public Mono<String> login(@RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout,
                              Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Неверное имя пользователя или пароль");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Вы успешно вышли из системы");
        }
        return Mono.just("login");
    }

    @GetMapping("/register")
    public Mono<String> registerForm(Model model) {
        model.addAttribute("user", new User());
        return Mono.just("register");
    }

    @PostMapping("/register")
    public Mono<String> registerUser(@ModelAttribute User user, Model model) {
        return userService.createUser(user.getUsername(), user.getPassword(), "USER")
                .then(Mono.just("redirect:/login?registered"))
                .onErrorResume(e -> {
                    model.addAttribute("errorMessage", "Ошибка регистрации: " + e.getMessage());
                    model.addAttribute("user", user);
                    return Mono.just("register");
                });
    }
}