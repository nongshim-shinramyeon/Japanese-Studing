package com.mysite.sbb.domain.user.service;

import com.mysite.sbb.domain.user.dto.AuthRequest;
import com.mysite.sbb.domain.user.dto.UserResponse;
import com.mysite.sbb.domain.user.entity.AppUser;
import com.mysite.sbb.domain.user.repository.AppUserRepository;
import com.mysite.sbb.global.exception.BusinessException;
import com.mysite.sbb.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordHashService passwordHashService;

    public AuthService(AppUserRepository appUserRepository, PasswordHashService passwordHashService) {
        this.appUserRepository = appUserRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public UserResponse signup(AuthRequest request) {
        String username = normalizeUsername(request.username());
        if (appUserRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        AppUser user = new AppUser(username, passwordHashService.hash(request.password()));
        return UserResponse.from(appUserRepository.save(user));
    }

    public UserResponse login(AuthRequest request) {
        String username = normalizeUsername(request.username());
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordHashService.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        return UserResponse.from(user);
    }

    public AppUser getUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}
