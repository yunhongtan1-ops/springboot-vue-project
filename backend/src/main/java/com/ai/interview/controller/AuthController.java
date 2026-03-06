package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.dto.AuthLoginRequest;
import com.ai.interview.dto.AuthRegisterRequest;
import com.ai.interview.service.AuthService;
import com.ai.interview.vo.LoginVO;
import com.ai.interview.vo.UserVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserVO> register(@RequestBody AuthRegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody AuthLoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
