import React from 'react';
import { XCircle, ArrowLeft } from 'lucide-react';

interface PaymentCancelPageProps {
  onReturnToGame: () => void;
}

export const PaymentCancelPage: React.FC<PaymentCancelPageProps> = ({ onReturnToGame }) => {
  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-radial from-slate-900 via-gray-950 to-black">
      <div className="w-full max-w-md glass-panel p-8 rounded-3xl border border-gray-800 text-center shadow-2xl space-y-6">
        <div className="w-20 h-20 rounded-full bg-amber-500/20 text-amber-400 flex items-center justify-center mx-auto border border-amber-500/40">
          <XCircle className="w-10 h-10" />
        </div>

        <div>
          <h2 className="text-2xl font-black text-white">Payment Cancelled</h2>
          <p className="text-xs text-gray-400 mt-1 font-mono">
            Stripe Checkout session was cancelled. No charges were made.
          </p>
        </div>

        <button
          onClick={onReturnToGame}
          className="w-full py-3.5 rounded-xl bg-gray-800 hover:bg-gray-700 text-white font-bold text-sm shadow-lg transition flex items-center justify-center gap-2 cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Return to Game Hub</span>
        </button>
      </div>
    </div>
  );
};
