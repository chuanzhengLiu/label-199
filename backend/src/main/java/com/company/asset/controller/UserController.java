package com.company.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.asset.common.Result;
import com.company.asset.entity.User;
import com.company.asset.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String username) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(userService.page(pageParam, queryWrapper));
    }

    @GetMapping("/search")
    public Result<List<User>> search(@RequestParam(required = false) String keyword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("username", keyword).or().like("real_name", keyword));
        }
        queryWrapper.last("limit 50"); // Limit results to prevent huge payload
        return Result.success(userService.list(queryWrapper));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody User user) {
        user.setCreateTime(LocalDateTime.now());
        return Result.success(userService.save(user));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}
