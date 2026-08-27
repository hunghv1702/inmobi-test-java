// API Envelope Structure matching Spring Boot Backend @ControllerAdvice
export interface BaseResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface ApiErrorViolation {
  field: string;
  message: string;
}

export interface ApiErrorData {
  timestamp?: string;
  path?: string;
  violations?: ApiErrorViolation[];
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

export interface CurrentUser {
  email: string;
  score: number;
  turns: number;
}

export interface GuessRequest {
  number: number;
}

export interface GuessResponse {
  guess: number;
  serverNumber: number;
  isCorrect: boolean;
  score: number;
  turns: number;
}

export interface LeaderboardEntry {
  email: string;
  score: number;
}

export interface LeaderboardResponse {
  leaderboard: LeaderboardEntry[];
}

export interface PaymentCheckoutRequest {
  successUrl: string;
  cancelUrl: string;
}

export interface PaymentCheckoutResponse {
  provider: string;
  checkoutSessionId: string;
  checkoutUrl: string;
  status: string;
}

export interface ConfirmPaymentRequest {
  checkoutSessionId: string;
}

export interface PaymentConfirmationResponse {
  provider: string;
  checkoutSessionId: string;
  status: string;
  turns: number;
}
