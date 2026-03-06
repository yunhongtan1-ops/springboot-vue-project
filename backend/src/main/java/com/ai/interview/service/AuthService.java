package com.ai.interview.service;

import com.ai.interview.dto.AuthLoginRequest;
import com.ai.interview.dto.AuthRegisterRequest;
import com.ai.interview.entity.User;
import com.ai.interview.entity.UserRole;
import com.ai.interview.mapper.UserMapper;
import com.ai.interview.security.AppUserPrincipal;
import com.ai.interview.security.PasswordService;
import com.ai.interview.security.TokenService;
import com.ai.interview.vo.LoginVO;
import com.ai.interview.vo.UserVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserMapper userMapper,
            PasswordService passwordService,
            TokenService tokenService,
            AuthenticationManager authenticationManager
    ) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public UserVO register(AuthRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        validateAuthRequest(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String nickname = request.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = username;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordService.encode(request.getPassword()));
        user.setNickname(nickname.trim());
        user.setRole(UserRole.USER);
        userMapper.save(user);

        return toUserVO(user);
    }

    public LoginVO login(AuthLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        validateAuthRequest(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (passwordService.needsUpgrade(user.getPassword())) {
            String upgradedPassword = passwordService.encode(request.getPassword());
            userMapper.updatePasswordById(user.getId(), upgradedPassword);
            user.setPassword(upgradedPassword);
        }

        AppUserPrincipal principal = AppUserPrincipal.fromUser(user);
        String token = tokenService.generateToken(principal);
        return new LoginVO(token, principal.toUserVO());
    }

    public UserVO getCurrentUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        User user = userMapper.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        return toUserVO(user);
    }

    private void validateAuthRequest(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }

    private UserVO toUserVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getRole().name());
    }
}