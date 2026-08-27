export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/v1/auth/login',
    REGISTER: '/api/v1/auth/register',
    FORGOT_PASSWORD_REQUEST: '/api/v1/auth/forgot-password/request',
    FORGOT_PASSWORD_RESET: '/api/v1/auth/forgot-password/reset',
    ME: '/api/v1/me',
  },
  GAME: {
    GUESS: '/api/v1/guess',
    LEADERBOARD: '/api/v1/leaderboard',
  },
  PAYMENT: {
    CHECKOUT: '/api/v1/payments/turn-packages/checkout',
    CONFIRM: '/api/v1/payments/turn-packages/confirm',
  },
} as const;
