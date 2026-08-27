package com.hunghv.inmobitestjava.controller;

import com.hunghv.inmobitestjava.generated.api.GameApi;
import com.hunghv.inmobitestjava.generated.model.GuessApiResponse;
import com.hunghv.inmobitestjava.generated.model.GuessRequest;
import com.hunghv.inmobitestjava.generated.model.GuessResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardApiResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardResponse;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import com.hunghv.inmobitestjava.service.IGameService;
import com.hunghv.inmobitestjava.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GameController implements GameApi {

    private final IGameService gameService;
    private final ApiResponseMapper apiResponseMapper;

    @Override
    public ResponseEntity<GuessApiResponse> guess(GuessRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Attempting to guess number: userId={}, guess={}", userId, request.getNumber());
        GuessResponse response = gameService.guess(userId, request.getNumber());
        log.info("Successfully guessed number: userId={}, correct={}", userId, response.getCorrect());
        return ResponseEntity.ok(apiResponseMapper.toSuccessResponse(response));
    }

    @Override
    public ResponseEntity<LeaderboardApiResponse> leaderboard() {
        List<LeaderboardResponse> response = gameService.getLeaderboard();
        return ResponseEntity.ok(apiResponseMapper.toLeaderboardResponse(response));
    }
}
