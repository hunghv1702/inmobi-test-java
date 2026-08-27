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

let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

axiosClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<BaseResponse<unknown>>) => {
    const originalRequest = error.config as any;

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      const isAuthUrl =
        originalRequest.url?.includes('/auth/refresh') || originalRequest.url?.includes('/auth/login');

      if (isAuthUrl) {
        localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN);
        localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
        window.dispatchEvent(new Event('auth:unauthorized'));
        const serverMessage =
          error.response?.data?.message || error.message || 'Authentication failed';
        return Promise.reject(new Error(serverMessage));
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem(APP_CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
      if (!refreshToken) {
        localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN);
        window.dispatchEvent(new Event('auth:unauthorized'));
        return Promise.reject(error);
      }

      try {
        const refreshResponse = await axios.post<
          BaseResponse<{ accessToken: string; refreshToken: string; tokenType: string }>
        >(`${APP_CONFIG.API_BASE_URL}/api/v1/auth/refresh`, { refreshToken });

        const { accessToken, refreshToken: newRefreshToken } = refreshResponse.data.data;

        localStorage.setItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN, accessToken);
        localStorage.setItem(APP_CONFIG.STORAGE_KEYS.REFRESH_TOKEN, newRefreshToken);

        window.dispatchEvent(new CustomEvent('auth:tokenRefreshed', { detail: accessToken }));

        processQueue(null, accessToken);
        isRefreshing = false;

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN);
        localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
        window.dispatchEvent(new Event('auth:unauthorized'));

        return Promise.reject(refreshError);
      }
    }

    const serverMessage =
      error.response?.data?.message || error.message || 'An unexpected server error occurred';
    return Promise.reject(new Error(serverMessage));
  }
);
