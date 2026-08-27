package com.hunghv.inmobitestjava.service;

import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;

public interface IUserService {

    CurrentUserResponse getCurrentUser(Long userId);
}
