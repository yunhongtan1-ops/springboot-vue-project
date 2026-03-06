package com.ai.interview.service;

import com.ai.interview.dto.AuthLoginRequest;
import com.ai.interview.dto.AuthRegisterRequest;
import com.ai.interview.entity.User;
import com.ai.interview.mapper.UserMapper;
import com.ai.interview.vo.LoginVO;
import com.ai.interview.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserVO register(AuthRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        validateAuthRequest(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        String nickname = request.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = username;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setNickname(nickname.trim());
        userMapper.save(user);

        return toUserVO(user);
    }

    public LoginVO login(AuthLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        validateAuthRequest(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = "mock-token-" + user.getId() + "-" + System.currentTimeMillis();
        return new LoginVO(token, toUserVO(user));
    }

    private void validateAuthRequest(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    private UserVO toUserVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname());
    }
}
