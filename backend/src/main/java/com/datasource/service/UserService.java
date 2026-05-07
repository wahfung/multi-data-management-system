package com.datasource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.datasource.dto.LoginRequest;
import com.datasource.dto.LoginResponse;
import com.datasource.entity.User;

public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    User register(LoginRequest request);

    User getByUsername(String username);
}
