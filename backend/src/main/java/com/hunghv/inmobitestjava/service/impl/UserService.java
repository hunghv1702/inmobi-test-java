package com.hunghv.inmobitestjava.service.impl;

import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.exception.ResourceNotFoundException;
import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;
import com.hunghv.inmobitestjava.mapper.UserMapper;
import com.hunghv.inmobitestjava.repository.UserRepository;
import com.hunghv.inmobitestjava.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Long userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> userNotFound(userId));
        return userMapper.toCurrentUserResponse(user);
    }

    private static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("User %d was not found".formatted(userId));
    }
}
