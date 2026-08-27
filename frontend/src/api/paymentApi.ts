import { axiosClient } from './axiosClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import type { BaseResponse, PaymentCheckoutResponse, PaymentConfirmationResponse } from '../types/api';

export const paymentApi = {
  createCheckoutSession: async (
    successUrl: string,
    cancelUrl: string
  ): Promise<PaymentCheckoutResponse> => {
    const res = await axiosClient.post<BaseResponse<PaymentCheckoutResponse>>(
      API_ENDPOINTS.PAYMENT.CHECKOUT,
      {
        successUrl,
        cancelUrl,
      }
    );
    return res.data.data;
  },

  confirmPayment: async (checkoutSessionId: string): Promise<PaymentConfirmationResponse> => {
    const res = await axiosClient.post<BaseResponse<PaymentConfirmationResponse>>(
      API_ENDPOINTS.PAYMENT.CONFIRM,
      {
        checkoutSessionId,
      }
    );
    return res.data.data;
  },
};
