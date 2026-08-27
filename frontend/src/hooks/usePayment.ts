import { useState } from 'react';
import { paymentApi } from '../api/paymentApi';

export const usePayment = () => {
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentError, setPaymentError] = useState('');

  const initiateCheckout = async () => {
    setIsProcessing(true);
    setPaymentError('');
    try {
      const successUrl = `${window.location.origin}/payment/success?session_id={CHECKOUT_SESSION_ID}`;
      const cancelUrl = `${window.location.origin}/payment/cancel`;

      const checkout = await paymentApi.createCheckoutSession(successUrl, cancelUrl);
      window.location.href = checkout.checkoutUrl;
    } catch (err: unknown) {
      setPaymentError(err instanceof Error ? err.message : 'Failed to initiate checkout session');
      setIsProcessing(false);
    }
  };

  return {
    initiateCheckout,
    isProcessing,
    paymentError,
  };
};
