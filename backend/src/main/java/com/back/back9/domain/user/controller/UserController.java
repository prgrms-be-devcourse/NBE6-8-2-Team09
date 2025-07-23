package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserDto;
import com.back.back9.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/login")
    public String login() {
        return "login";
    }

    @GetMapping("/user/register")
    public String register() {
        return "register";
    }

    @PostMapping("/user/register")
    public String register(@ModelAttribute UserDto userDto, Model model) {
        boolean result = userService.register(userDto);
        if (!result) {
            model.addAttribute("error", "회원가입에 실패했습니다. 아이디/이름 중복 또는 비밀번호 불일치입니다.");
            return "register";
        }
        return "redirect:/user/login";
    }
}