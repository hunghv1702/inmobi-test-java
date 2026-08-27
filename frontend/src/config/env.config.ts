export const APP_CONFIG = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || '',
  STORAGE_KEYS: {
    AUTH_TOKEN: 'guess_royale_auth_token',
    REFRESH_TOKEN: 'guess_royale_refresh_token',
  },
  GAME: {
    MIN_NUMBER: 1,
    MAX_NUMBER: 5,
    DEFAULT_SELECTION: 3,
    TURN_PACKAGE_COST: '$1.99',
    TURNS_PER_PACKAGE: 5,
  },
} as const;
