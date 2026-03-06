package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.dto.AuthLoginRequest;
import com.ai.interview.dto.AuthRegisterRequest;
import com.ai.interview.security.AppUserPrincipal;
import com.ai.interview.service.AuthService;
import com.ai.interview.vo.LoginVO;
import com.ai.interview.vo.UserVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/me")
    public ApiResponse<UserVO> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.success(authService.getCurrentUser(principal.getUsername()));
    }
}