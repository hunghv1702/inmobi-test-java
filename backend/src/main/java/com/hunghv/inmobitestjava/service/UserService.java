package com.hunghv.inmobitestjava.service;

import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;

public interface UserService {

    CurrentUserResponse getCurrentUser(Long userId);

    CurrentUserResponse addTurns(Long userId, int amount);
}
