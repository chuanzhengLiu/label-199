package com.company.asset.controller;

import com.company.asset.common.Result;
import com.company.asset.entity.User;
import com.company.asset.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User user = userService.login(username, password);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("Invalid username or password");
    }
}
