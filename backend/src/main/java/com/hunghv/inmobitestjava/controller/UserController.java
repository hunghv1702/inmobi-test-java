package com.hunghv.inmobitestjava.controller;

import com.hunghv.inmobitestjava.generated.api.UserApi;
import com.hunghv.inmobitestjava.generated.model.CurrentUserApiResponse;
import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import com.hunghv.inmobitestjava.service.IUserService;
import com.hunghv.inmobitestjava.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final IUserService userService;
    private final ApiResponseMapper apiResponseMapper;

    @Override
    public ResponseEntity<CurrentUserApiResponse> me() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Attempting to get current user: userId={}", userId);
        CurrentUserResponse response = userService.getCurrentUser(userId);
        log.info("Successfully got current user: userId={}", userId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(apiResponseMapper.toSuccessResponse(response));
    }
}
