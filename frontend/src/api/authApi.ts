import { axiosClient } from './axiosClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import type { BaseResponse, AuthResponse, CurrentUser } from '../types/api';

export interface ForgotPasswordResponseData {
  email: string;
  message: string;
}

export const authApi = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const res = await axiosClient.post<BaseResponse<AuthResponse>>(API_ENDPOINTS.AUTH.LOGIN, {
      email,
      password,
    });
    return res.data.data;
  },

  register: async (email: string, password: string): Promise<CurrentUser> => {
    const res = await axiosClient.post<BaseResponse<CurrentUser>>(API_ENDPOINTS.AUTH.REGISTER, {
      email,
      password,
    });
    return res.data.data;
  },

  requestForgotPasswordOtp: async (email: string): Promise<ForgotPasswordResponseData> => {
    const res = await axiosClient.post<BaseResponse<ForgotPasswordResponseData>>(
      API_ENDPOINTS.AUTH.FORGOT_PASSWORD_REQUEST,
      { email }
    );
    return res.data.data;
  },

  resetPasswordWithOtp: async (
    email: string,
    otp: string,
    newPassword: string
  ): Promise<ForgotPasswordResponseData> => {
    const res = await axiosClient.post<BaseResponse<ForgotPasswordResponseData>>(
      API_ENDPOINTS.AUTH.FORGOT_PASSWORD_RESET,
      { email, otp, newPassword }
    );
    return res.data.data;
  },

  getMe: async (): Promise<CurrentUser> => {
    const res = await axiosClient.get<BaseResponse<CurrentUser>>(API_ENDPOINTS.AUTH.ME);
    return res.data.data;
  },
};
