import { axiosClient } from './axiosClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import type { BaseResponse, GuessResponse, LeaderboardResponse } from '../types/api';

export const gameApi = {
  guess: async (number: number): Promise<GuessResponse> => {
    const res = await axiosClient.post<BaseResponse<GuessResponse>>(API_ENDPOINTS.GAME.GUESS, {
      number,
    });
    return res.data.data;
  },

  getLeaderboard: async (): Promise<LeaderboardResponse> => {
    const res = await axiosClient.get<BaseResponse<LeaderboardResponse>>(
      API_ENDPOINTS.GAME.LEADERBOARD
    );
    return res.data.data;
  },
};
