package com.company.asset.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.asset.entity.User;
import com.company.asset.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    public User login(String username, String password) {
        return lambdaQuery().eq(User::getUsername, username).eq(User::getPassword, password).one();
    }
}
