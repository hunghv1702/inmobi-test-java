package com.hunghv.inmobitestjava.service.impl;

import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.exception.ResourceNotFoundException;
import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;
import com.hunghv.inmobitestjava.mapper.UserMapper;
import com.hunghv.inmobitestjava.repository.UserRepository;
import com.hunghv.inmobitestjava.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Long userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> userNotFound(userId));
        return userMapper.toCurrentUserResponse(user);
    }

    @Override
    @Transactional
    public CurrentUserResponse addTurns(Long userId, int amount) {
        UserAccount user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> userNotFound(userId));
        user.addTurns(amount);
        return userMapper.toCurrentUserResponse(user);
    }

    private static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("User %d was not found".formatted(userId));
    }
}
