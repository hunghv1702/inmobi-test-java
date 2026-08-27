import axios, { AxiosError } from 'axios';
import { APP_CONFIG } from '../config/env.config';
import type { BaseResponse } from '../types/api';

export const axiosClient = axios.create({
  baseURL: APP_CONFIG.API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<BaseResponse<unknown>>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN);
      window.dispatchEvent(new Event('auth:unauthorized'));
    }

    const serverMessage =
      error.response?.data?.message || error.message || 'An unexpected server error occurred';
    return Promise.reject(new Error(serverMessage));
  }
);
