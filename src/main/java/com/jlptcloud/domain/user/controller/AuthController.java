package com.jlptcloud.domain.user.controller;

import com.jlptcloud.domain.user.dto.AuthRequest;
import com.jlptcloud.domain.user.dto.UserResponse;
import com.jlptcloud.domain.user.service.AuthService;
import com.jlptcloud.global.api.ApiResponse;
import com.jlptcloud.global.exception.BusinessException;
import com.jlptcloud.global.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_ID = "USER_ID";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody AuthRequest request, HttpSession session) {
        UserResponse user = authService.signup(request);
        session.setAttribute(SESSION_USER_ID, user.id());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        UserResponse user = authService.login(request);
        session.setAttribute(SESSION_USER_ID, user.id());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(authService.getUser(userId))));
    }
}
