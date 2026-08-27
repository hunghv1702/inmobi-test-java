package com.hunghv.inmobitestjava.service;

import com.hunghv.inmobitestjava.generated.model.GuessResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardResponse;

import java.util.List;

public interface IGameService {

    GuessResponse guess(Long userId, int guess);

    List<LeaderboardResponse> getLeaderboard();
}
