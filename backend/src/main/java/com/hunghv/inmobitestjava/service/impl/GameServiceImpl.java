package com.hunghv.inmobitestjava.service.impl;

import com.hunghv.inmobitestjava.constant.GameConstant;
import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.exception.ResourceNotFoundException;
import com.hunghv.inmobitestjava.generated.model.GuessResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardResponse;
import com.hunghv.inmobitestjava.mapper.UserMapper;
import com.hunghv.inmobitestjava.repository.UserRepository;
import com.hunghv.inmobitestjava.service.GameService;
import com.hunghv.inmobitestjava.service.RandomNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final UserRepository userRepository;
    private final RandomNumberGenerator randomNumberGenerator;
    private final UserMapper userMapper;

    @Value("${app.game.win-rate:0.05}")
    private double winRate;

    @Override
    @Transactional
    public GuessResponse guess(Long userId, int guess) {
        UserAccount user = getUserForUpdate(userId);
        user.consumeTurn();

        boolean shouldWin = ThreadLocalRandom.current().nextDouble() < winRate;
        int serverNumber;
        if (shouldWin) {
            serverNumber = guess;
        } else {
            List<Integer> wrongNumbers = new ArrayList<>();
            for (int i = GameConstant.MIN_NUMBER; i <= GameConstant.MAX_NUMBER; i++) {
                if (i != guess) {
                    wrongNumbers.add(i);
                }
            }
            int randomIndex = ThreadLocalRandom.current().nextInt(wrongNumbers.size());
            serverNumber = wrongNumbers.get(randomIndex);
        }

        boolean correct = guess == serverNumber;
        if (correct) {
            user.increaseScore();
        }

        log.info("Guess processed (winRate={}): userId={}, guess={}, serverNumber={}, correct={}, score={}, turns={}",
                winRate, userId, guess, serverNumber, correct, user.getScore(), user.getTurns());
        return userMapper.toGuessResponse(user, guess, serverNumber, correct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard() {
        return userMapper.toLeaderboardResponses(userRepository.findTop10ByOrderByScoreDescIdAsc());
    }

    private UserAccount getUserForUpdate(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> userNotFound(userId));
    }

    private static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("User %d was not found".formatted(userId));
    }
}
